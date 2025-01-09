package net.cofidayo.firstjoinmessage;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Firstjoinmessage extends JavaPlugin implements Listener {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        // 設定ファイルを生成・読み込み
        saveDefaultConfig();
        config = getConfig();

        // イベントリスナーを登録
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("FirstJoinMessage プラグインが有効になりました。");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 初参加かどうかを判定
        if (!player.hasPlayedBefore()) {
            // 設定からメッセージリストを取得
            List<String> messages = config.getStringList("message");

            for (String line : messages) {
                // プレイヤー名を置換
                line = line.replace("{player}", player.getName());

                // マークダウン形式のリンクを解析してクリック可能に
                TextComponent component = parseMarkdown(line);

                // プレイヤーに送信
                player.spigot().sendMessage(component);
            }
        }
    }

    /**
     * マークダウン形式のテキストを解析し、クリック可能なリンクを含むTextComponentを生成
     *
     * @param text 入力テキスト
     * @return TextComponent
     */
    private TextComponent parseMarkdown(String text) {
        TextComponent baseComponent = new TextComponent();

        // カラーコードを適用
        text = ChatColor.translateAlternateColorCodes('&', text);

        // マークダウンリンクの正規表現: [テキスト](URL)
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            // マッチ前の通常テキスト部分
            if (matcher.start() > lastEnd) {
                String normalText = text.substring(lastEnd, matcher.start());
                baseComponent.addExtra(new TextComponent(normalText));
            }

            // マークダウンリンク部分
            String linkText = matcher.group(1); // [テキスト]
            String url = matcher.group(2);     // (URL)

            TextComponent linkComponent = new TextComponent(linkText);
            linkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            baseComponent.addExtra(linkComponent);

            lastEnd = matcher.end();
        }

        // 残りの通常テキスト部分
        if (lastEnd < text.length()) {
            baseComponent.addExtra(new TextComponent(text.substring(lastEnd)));
        }

        return baseComponent;
    }

    /**
     * コマンド処理
     *
     * @param sender コマンドの送信者
     * @param command コマンド名
     * @param label ラベル
     * @param args コマンド引数
     * @return 成功した場合はtrue
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fjm")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                // 設定ファイルを再読み込み
                reloadConfig();
                config = getConfig();
                sender.sendMessage(ChatColor.GREEN + "FirstJoinMessageの設定をリロードしました。");
                return true;
            }
        }
        return false;
    }
}
