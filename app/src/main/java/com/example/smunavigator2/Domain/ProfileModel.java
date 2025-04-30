package com.example.smunavigator2.Domain;

import java.util.ArrayList;

public class ProfileModel
{
    public String profileName;
    public String profileImage;
    public String department;
    public String about;
    public String followersNum;
    public String followingNum;
    public String likes;
    public ArrayList<Follower> followers;
    public ArrayList<Post> posts;


    public ProfileModel() {

    }

    public ProfileModel(String profileName,
                        String profileImage,
                        String department,
                        String about,
                        String followersNum,
                        String followingNum,
                        String likes,
                        ArrayList<Follower> followers,
                        ArrayList<Post> posts)
    {
        this.profileName = profileName;
        this.followers = followers;
        this.likes = likes;
        this.followingNum = followingNum;
        this.followersNum = followersNum;
        this.about = about;
        this.department = department;
        this.profileImage = profileImage;
        this.posts = posts;
    }

    public static class Follower
    {
        public String imageUrl;
        public String name;


        public Follower()
        {

        }

        public Follower(String imageUrl, String name) {
            this.imageUrl = imageUrl;
            this.name = name;
        }
    }

    public static class Post{
        public String imageUrl;

        public Post()
        {

        }

        public Post(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

}
