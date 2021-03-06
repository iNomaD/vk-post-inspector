# vk-post-inspector
Utility allows to get a list of users who liked, reposted or commented a specific post in a group according to selected filters

#Getting Started
Release is situated in `dist` folder. Java 8 is required!

Run jar with `java -jar vk-post-inspector-jar` or `start.bat` file. Initially, `conf.properties` file should be edited to set parameters of the parser shown below.

1. `ownerId` (required) - id of the group that contains the post
2. `itemId` (required) - id the post
3. `isWoman` - boolean value for gender filter
3. `cityId` - integer value for city, e.g. 1 for Moscow and 2 for SPB
4. `minAge` and `maxAge` - integer values for age
5. `advancedAge` - boolean value (attempts to recognize age if it's hidden at the userpage, needs accessToken)
5. `appId`, `accessToken`, `userId` - values requred to get access to the post in the closed group and for `advancedAge`, you can use default app with id `5810259` and simply run jar with `-token` parameter to get `accessToken` and `userId`
6. `outputToFile` - boolean and `outputName` - String value to specify where the result will be placed

# Development Environment
Project is built with gradle and uses official VK Java SDK

# Others
A related utility [vk-post-searcher](https://github.com/iNomaD/VKpostSearcher), otherwise, scans a list of group for posts and comments with a regex or posted by a specific user.
