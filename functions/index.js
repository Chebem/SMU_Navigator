// Follow notifications: when someone follows you, add it to your in-app list and push it to your phone.
const {onValueCreated} = require("firebase-functions/v2/database");
const {initializeApp} = require("firebase-admin/app");
const {getDatabase} = require("firebase-admin/database");
const {getMessaging} = require("firebase-admin/messaging");

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
