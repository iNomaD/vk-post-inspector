import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.users.UserXtrCounters;

import java.util.List;

/**
 * Created by Denis on 02.01.2017.
 */
public class Auth {
    public static UserActor authorize(VkApiClient vk, int userId, String accessToken){
        UserActor userActor = new UserActor(userId, accessToken);
        try{
            List<UserXtrCounters> userXtrCounterses = vk.users().get(userActor).execute();
            UserXtrCounters currentUser = userXtrCounterses.get(0);
            System.out.println("Current user: "+currentUser.getFirstName()+" "+currentUser.getLastName()+" "+currentUser.getId());
        }
        catch(Throwable e){
            System.out.println("Using app without access token. Get it through url");
            System.out.println("https://oauth.vk.com/authorize?client_id="+Config.appId+"&redirect_url=https://oauth.vk.com/blank.html&response_type=token&scope=offline");
            //Desktop.getDesktop().browse(new URI("https://oauth.vk.com/authorize?client_id="+Config.appId+"&redirect_url=https://oauth.vk.com/blank.html&response_type=token&scope=offline"));
            return new UserActor(0, "");
        }
        return userActor;
    }
}
