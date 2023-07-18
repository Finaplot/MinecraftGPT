package it.ohalee.minecraftgpt;


import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatHandler implements Listener {

    private final Main plugin;
    private final Map<UUID, String> playerMessages;
    private final boolean sendMessagesToConsole;
    private final boolean useDefaultChat;
    private final List<String> chatFormat;

    public ChatHandler(Main plugin, boolean sendMessagesToConsole, boolean useDefaultChat, List<String> chatFormat) {
        this.plugin = plugin;
        this.playerMessages = new HashMap<>();
        this.sendMessagesToConsole = sendMessagesToConsole;
        this.useDefaultChat = useDefaultChat;
        this.chatFormat = chatFormat;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        // 获取玩家和聊天内容
        Player player = event.getPlayer();
        String message = event.getMessage();

        // 对消息进行处理和转换
        String responseMessage = StringEscapeUtils.unescapeJava(message);

        // 判断玩家是否开启了ChatGPT模式
        if (Main.CACHE.getIfPresent(player) != null) {
            // 保存玩家的聊天信息
            playerMessages.put(player.getUniqueId(), message);

            // 取消事件，不让其他玩家看到聊天内容
            event.setCancelled(true);

            try {
                String api = plugin.getConfig().getString("api");
                if (api != null && api.equalsIgnoreCase("openai")) {
                    // 使用 OpenAI 的官方接口
                    // 添加你的 OpenAI 相关代码
                    // ...
                } else {
                    // 使用 CloseAI 的接口
                    // 创建URL对象
                    URL url = new URL("https://api.closeai-proxy.xyz/v1/chat/completions");

                    // 创建HttpURLConnection对象
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // 设置请求方法为POST
                    connection.setRequestMethod("POST");

                    // 添加请求头
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer sk-b2ZIwF65hWdJofefO7fFxEPbVn0de968YqTwk6X6bXeGWEPa");

                    // 构建请求体
                    String requestBody = "{\"model\": \"gpt-3.5-turbo\", " +
                            "\"messages\": [{\"role\": \"system\", \"content\": \"玩家：" + message + "\"}," +
                            "{\"role\": \"user\", \"content\": \"" + message + "\"}]}";

                    // 启用输出流，将请求体写入连接
                    connection.setDoOutput(true);
                    connection.getOutputStream().write(requestBody.getBytes("UTF-8"));

                    // 发送请求
                    int responseCode = connection.getResponseCode();

                    // 读取响应
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // 解析响应结果
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    JSONObject messageObject = choices.getJSONObject(0).getJSONObject("message");
                    responseMessage = StringEscapeUtils.unescapeJava(messageObject.getString("content"));

                    // 发送玩家的消息给玩家
                    player.sendMessage(ChatColor.YELLOW + "玩家：" + message);

                    // 发送响应消息给玩家
                    player.sendMessage(ChatColor.YELLOW + "GPT：" + responseMessage);

                    // 关闭连接
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 发送玩家自己的消息
            player.sendMessage(ChatColor.YELLOW + "玩家：" + message);
        }

        // 发送消息给控制台
        if (sendMessagesToConsole) {
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "玩家 " + player.getName() + " 说：" + message);
        }

        // 使用默认聊天格式发送消息
        if (useDefaultChat) {
            for (String format : chatFormat) {
                String formattedMessage = format
                        .replace("%player%", player.getName())
                        .replace("%message%", message);
                plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', formattedMessage));
            }
        }
    }

    public String getPlayerMessage(UUID playerUUID) {
        return playerMessages.getOrDefault(playerUUID, "");
    }
}
