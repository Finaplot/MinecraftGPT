package it.ohalee.minecraftgpt;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

// 在ChatHandler类中处理玩家的聊天事件，使用curl命令来调用CloseAI的API接口
public class ChatHandler implements Listener {

    private final Main plugin;

    public ChatHandler(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        // 获取玩家和聊天内容
        Player player = event.getPlayer();
        String message = event.getMessage();
        // 判断玩家是否开启了ChatGPT模式
        if (Main.CACHE.getIfPresent(player) != null) {
            try {
                // 创建一个curl命令，调用CloseAI的API接口，传入聊天内容
                String command = "curl -H \"Authorization: Bearer sk-aBsUfypK9tkJnc5zBU7j9RyGaTo5N3TJNU1Y7WcOac9ldz7U\" -H \"Content-Type: application/json\" -d \"{\\\"model\\\":" +
                        "\\\"gpt-3.5-turbo\\\",\\\"messages\\\":[{\\\"role\\\":\\\"user\\\",\\\"content\\\":\\\"Hellow!\\\"}]}\" " +
                        "https://api.closeai-asia.com/v1/chat/completions";
                // 使用Runtime类来执行curl命令
                Process process = Runtime.getRuntime().exec(command);
                // 获取返回的结果
                InputStream inputStream = process.getInputStream();
                // 转换成字符串
                String result = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
                // 转换成JSON对象
                JSONObject json = new JSONObject(result);
                // 提取出聊天内容和置信度
                String response = json.getString("response");
                double confidence = json.getDouble("confidence");
                // 给玩家发送回复，带上置信度
                player.sendMessage(ChatColor.GRAY + "[ChatGPT] " + ChatColor.WHITE + response + ChatColor.GRAY + " (" + confidence + ")");
                // 取消事件，不让其他玩家看到聊天内容
                event.setCancelled(true);
            } catch (Exception e) {
                // 处理异常
                e.printStackTrace();
            }
        }
    }
}
