package net.maplemc.lyttleessentials.types;

import net.maplemc.lyttleessentials.LyttleEssentials;
import org.bukkit.entity.Player;

public class Invoice {
    private final LyttleEssentials plugin;

    public Invoice(LyttleEssentials plugin) {
        this.plugin = plugin;
    }

    public Bill teleportToHome(Player player) {
        int price = (int) this.plugin.config.general.get("invoices.teleport_to_home.price");
        boolean increase = (boolean) this.plugin.config.general.get("invoices.teleport_to_home.increase");
        return charge(player, "teleport_to_home", price, increase, false);
    }

    public Bill createWarp(Player player) {
        int price = (int) this.plugin.config.general.get("invoices.create_warp.price");
        boolean increase = (boolean) this.plugin.config.general.get("invoices.create_warp.increase");
        return charge(player, "create_warp", price, increase, false);
    }

    public Bill teleportToWarp(Player player) {
        int price = (int) this.plugin.config.general.get("invoices.teleport_to_warp.price");
        boolean increase = (boolean) this.plugin.config.general.get("invoices.teleport_to_warp.increase");
        return charge(player, "teleport_to_warp", price, increase, false);
    }

    public Bill teleportToSpawn(Player player) {
        int price = (int) this.plugin.config.general.get("invoices.teleport_to_spawn.price");
        boolean increase = (boolean) this.plugin.config.general.get("invoices.teleport_to_spawn.increase");
        return charge(player, "teleport_to_spawn", price, increase, false);
    }

    public Bill teleportToPlayer(Player player) {
        int price = (int) this.plugin.config.general.get("invoices.teleport_to_player.price");
        boolean increase = (boolean) this.plugin.config.general.get("invoices.teleport_to_player.increase");
        return charge(player, "teleport_to_player", price, increase, false);
    }

    public Bill teleportToPlayerCheck(Player player) {
        int price = (int) this.plugin.config.general.get("invoices.teleport_to_player.price");
        boolean increase = (boolean) this.plugin.config.general.get("invoices.teleport_to_player.increase");
        return charge(player, "teleport_to_player", price, increase, true);
    }

    private void clearOrCreateInvoiceDay() {
        String date = (String) this.plugin.config.invoices.get("date");
        // Create a data string with only year, month, and day
        String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        if (date == null || !date.equals(todayStr)) {
            this.plugin.config.invoices.clear();
            this.plugin.config.invoices.set("date", todayStr);
        }
    }

    private Bill charge(Player player, String type, int price, boolean increase, boolean checkOnly) {
        clearOrCreateInvoiceDay();

        String key = player.getUniqueId() + "." + type;
        Object billedAmountObj = this.plugin.config.invoices.get(key);
        int billedAmount = (int) (billedAmountObj != null ? billedAmountObj : 0);
        int payPrice = increase ? ((billedAmount > 0) ? (price * billedAmount) : price) : price;

        double balance = this.plugin.economyImplementer.getBalance(player);
        if (balance < payPrice) {
            return new Bill(-1, payPrice);
        }

        if (checkOnly) {
            return new Bill(payPrice, payPrice + price);
        }

        this.plugin.economyImplementer.withdrawPlayer(player, payPrice);
        this.plugin.config.invoices.set(key, billedAmount + 1);
        return new Bill(payPrice, payPrice + price);
    }
}
