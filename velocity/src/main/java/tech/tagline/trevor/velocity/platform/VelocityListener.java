package tech.tagline.trevor.velocity.platform;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import tech.tagline.trevor.common.proxy.DatabaseProxyImpl;
import tech.tagline.trevor.velocity.TrevorVelocity;

public class VelocityListener {

  private final TrevorVelocity plugin;

  public VelocityListener(TrevorVelocity plugin) {
    this.plugin = plugin;
  }

  @Subscribe
  public void onPlayerConnect(LoginEvent event) {
    Player player = event.getPlayer();

    // TODO: Maybe keep a map of platform users
    VelocityUser user = new VelocityUser(player);

    DatabaseProxyImpl.ConnectResult result =
            plugin.getCommon().getDatabaseProxy().onPlayerConnect(user);

    if (!result.isAllowed()) {
      event.setResult(
              ResultedEvent.ComponentResult.denied(serialize(result.getMessage().orElse("")))
      );
    }
  }

  @Subscribe
  public void onPlayerDisconnect(DisconnectEvent event) {
    Player player = event.getPlayer();
    // TODO: Maybe keep a map of platform users
    VelocityUser user = new VelocityUser(player);

    plugin.getCommon().getDatabaseProxy().onPlayerDisconnect(user);
  }

  @Subscribe
  public void onPlayerServerChange(ServerConnectedEvent event) {
    String server = event.getServer().getServerInfo().getName();
    String previousServer = "";
    if (event.getPreviousServer().isPresent()) {
      previousServer = event.getPreviousServer().get().getServerInfo().getName();
    }

    Player player = event.getPlayer();
    // TODO: Maybe keep a map of platform users
    VelocityUser user = new VelocityUser(player);

    plugin.getCommon().getDatabaseProxy().onPlayerServerChange(user, server, previousServer);
  }

  @Subscribe
  public void onProxyPing(ProxyPingEvent event) {
    ServerPing ping = event.getPing();
    ServerPing.Builder builder = ping.asBuilder();

    builder.onlinePlayers(plugin.getCommon().getInstanceData().getPlayerCount());

    event.setPing(builder.build());
  }

  @Subscribe(order = PostOrder.LAST)
  public void onServerPing(ProxyPingEvent event) {
    event.setPing(
            event.getPing().asBuilder()
                    .onlinePlayers(plugin.getCommon().getInstanceData().getPlayerCount()).build()
    );
  }

  private Component serialize(String text) {
    return LegacyComponentSerializer.legacy().deserialize(text, '&');
  }
}
