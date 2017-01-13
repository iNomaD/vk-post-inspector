package edu.etu.vkpi;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.users.UserXtrCounters;

import java.awt.*;
import java.net.URI;
import java.util.List;

public class Auth {
    public static UserActor authorize(VkApiClient vk, int userId, String accessToken){
        UserActor userActor = new UserActor(userId, accessToken);
        try{
            List<UserXtrCounters> userXtrCounterses = vk.users().get(userActor).execute();
            UserXtrCounters currentUser = userXtrCounterses.get(0);
            System.out.println("Current user: "+currentUser.getFirstName()+" "+currentUser.getLastName()+" "+currentUser.getId());
        }
        catch(Throwable e){
            System.out.println("Using app without access token. Get it through -token launch parameter");
            return new UserActor(0, "");
        }
        return userActor;
    }

    public static void getToken(){
        try {
            if(Config.appId == null || Config.appId == 0){
                System.out.println("appId is not specified!");
                System.out.println("Please, set appId in "+Config.CONF_FILE_NAME + " and restart application");
                System.in.read();
                return;
            }
            System.out.println("You need to provide permissions for the application");
            String url = "https://oauth.vk.com/authorize?client_id="+Config.appId+"&redirect_url=https://oauth.vk.com/blank.html&response_type=token&scope=offline,wall";
            System.out.println(url);
            System.out.println("Please, copy 'access_token' and 'user_id' from your url to "+Config.CONF_FILE_NAME+" and restart application");
            Desktop.getDesktop().browse(new URI(url));
            System.in.read();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
