// Follow notifications: when someone follows you, add it to your in-app list and push it to your phone.
// Account deletion: when a login is deleted, remove everything that belongs to it.
const {onValueCreated} = require("firebase-functions/v2/database");
const functionsV1 = require("firebase-functions/v1"); // 2nd gen has no "user deleted" trigger
const {initializeApp} = require("firebase-admin/app");
const {getDatabase} = require("firebase-admin/database");
const {getMessaging} = require("firebase-admin/messaging");
const {getStorage} = require("firebase-admin/storage");

initializeApp();

const ONE_HOUR = 60 * 60 * 1000;
const STALE_TOKEN_ERRORS = [
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
];

exports.notifyNewFollower = onValueCreated(
  {
    ref: "/profiles/{uid}/followers/{followerUid}",
    instance: "smu-navigator-default-rtdb",
    region: "asia-southeast1", // must match the database location
  },
  async (event) => {
    const {uid, followerUid} = event.params;
    if (uid === followerUid) return;

    const db = getDatabase();
    // One entry per follower, so follow / unfollow / follow doesn't pile up duplicates
    const entryRef = db.ref(`notifications/${uid}/follow_${followerUid}`);
    const previous = (await entryRef.get()).val();
    const now = Date.now();
    await entryRef.set({type: "follow", fromUid: followerUid, timestamp: now, read: false});

    // Followed again within an hour: the list is updated, but don't push twice
    if (previous && now - (previous.timestamp || 0) < ONE_HOUR) return;

    const tokens = Object.keys((await db.ref(`fcmTokens/${uid}`).get()).val() || {});
    if (tokens.length === 0) return;
    const name = (await db.ref(`profiles/${followerUid}/profileName`).get()).val() || "Someone";

    const result = await getMessaging().sendEachForMulticast({
      tokens,
      // English fallback; Android shows the app's own string (English or Korean) via the loc keys
      notification: {title: "New follower", body: `${name} started following you`},
      android: {
        notification: {
          channelId: "general",
          titleLocKey: "notif_follow_title",
          bodyLocKey: "notif_follow_text",
          bodyLocArgs: [name],
        },
      },
      data: {type: "follow", fromUid: followerUid},
    });

    // Remove tokens from phones that uninstalled the app or reset their token
    const stale = {};
    result.responses.forEach((r, i) => {
      if (!r.success && STALE_TOKEN_ERRORS.includes(r.error && r.error.code)) stale[tokens[i]] = null;
    });
    if (Object.keys(stale).length > 0) await db.ref(`fcmTokens/${uid}`).update(stale);
  },
);

// Runs after the app deletes the login (Profile -> Settings -> Delete account, or an email request done in the console)
exports.cleanUpDeletedUser = functionsV1
  .region("asia-southeast1")
  .auth.user()
  .onDelete(async (user) => {
    const uid = user.uid;
    const db = getDatabase();
    const updates = {};

    const profile = (await db.ref(`profiles/${uid}`).get()).val() || {};
    // Their follows, on both sides
    Object.keys(profile.following || {}).forEach((other) => {
      updates[`profiles/${other}/followers/${uid}`] = null;
    });
    Object.keys(profile.followers || {}).forEach((other) => {
      updates[`profiles/${other}/following/${uid}`] = null;
    });

    // Their likes and comments on other people's posts
    const profiles = (await db.ref("profiles").get()).val() || {};
    Object.entries(profiles).forEach(([owner, p]) => {
      if (owner === uid) return;
      Object.entries((p && p.posts) || {}).forEach(([postId, post]) => {
        if (post && post.likes && post.likes[uid]) {
          updates[`profiles/${owner}/posts/${postId}/likes/${uid}`] = null;
        }
        Object.entries((post && post.comments) || {}).forEach(([commentId, c]) => {
          if (c && c.userId === uid) updates[`profiles/${owner}/posts/${postId}/comments/${commentId}`] = null;
        });
      });
    });

    // "X started following you" entries they caused
    const notifications = (await db.ref("notifications").get()).val() || {};
    Object.keys(notifications).forEach((other) => {
      if (notifications[other] && notifications[other][`follow_${uid}`]) {
        updates[`notifications/${other}/follow_${uid}`] = null;
      }
    });

    // Their reviews of places
    const reviews = (await db.ref("reviews").get()).val() || {};
    Object.keys(reviews).forEach((placeKey) => {
      if (reviews[placeKey] && reviews[placeKey][uid]) updates[`reviews/${placeKey}/${uid}`] = null;
    });

    // Everything of their own
    updates[`profiles/${uid}`] = null;
    updates[`blocks/${uid}`] = null;
    updates[`notifications/${uid}`] = null;
    updates[`fcmTokens/${uid}`] = null;
    updates[`favorites/${uid}`] = null;
    await db.ref().update(updates);

    // Profile photo and post photos
    const bucket = getStorage().bucket();
    await Promise.all([
      bucket.deleteFiles({prefix: `profileImages/${uid}/`}),
      bucket.deleteFiles({prefix: `posts/${uid}/`}),
    ]);
  });
