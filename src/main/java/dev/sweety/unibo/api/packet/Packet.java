package dev.sweety.unibo.api.packet;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientConfigurationEndAck;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientSelectKnownPacks;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginSuccessAck;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import lombok.Getter;

import java.util.Arrays;

@Getter
public final class Packet {

    private final long timestamp;

    private final ProtocolPacketEvent event;

    private final PacketWrapper<?> wrapper;

    private final PacketType type;

    public Packet(final PacketSendEvent send) {
        this.event = send;

        this.timestamp = send.getTimestamp();
        final PacketType.PacketServer type = PacketType.PacketServer.valueOf(send.getPacketType().getName());

        this.type = type;

        this.wrapper = switch (type) {
            case ENTITY_EQUIPMENT -> new WrapperPlayServerEntityEquipment(send);
            case CHANGE_GAME_STATE -> new WrapperPlayServerChangeGameState(send);
            case ENTITY_MOVEMENT -> new WrapperPlayServerEntityMovement(send);
            case SPAWN_LIVING_ENTITY -> new WrapperPlayServerSpawnLivingEntity(send);
            case CHAT_MESSAGE -> new WrapperPlayServerChatMessage(send);
            case COMBAT_EVENT -> new WrapperPlayServerCombatEvent(send);
            case ENTITY_STATUS -> new WrapperPlayServerEntityStatus(send);
            case EXPLOSION -> new WrapperPlayServerExplosion(send);
            case JOIN_GAME -> new WrapperPlayServerJoinGame(send);
            case MAP_DATA -> new WrapperPlayServerMapData(send);
            case PLAYER_ABILITIES -> new WrapperPlayServerPlayerAbilities(send);
            case ENTITY_EFFECT -> new WrapperPlayServerEntityEffect(send);
            case SET_SLOT -> new WrapperPlayServerSetSlot(send);
            case SET_CURSOR_ITEM -> new WrapperPlayServerSetCursorItem(send);
            case TEAMS -> new WrapperPlayServerTeams(send);
            case WINDOW_ITEMS -> new WrapperPlayServerWindowItems(send);
            case DAMAGE_EVENT -> new WrapperPlayServerDamageEvent(send);
            case RESPAWN -> new WrapperPlayServerRespawn(send);
            case REMOVE_ENTITY_EFFECT -> new WrapperPlayServerRemoveEntityEffect(send);
            case DEATH_COMBAT_EVENT -> new WrapperPlayServerDeathCombatEvent(send);
            case ENTITY_TELEPORT -> new WrapperPlayServerEntityTeleport(send);
            case ENTITY_VELOCITY -> new WrapperPlayServerEntityVelocity(send);
            case UPDATE_HEALTH -> new WrapperPlayServerUpdateHealth(send);
            case PLAYER_POSITION_AND_LOOK -> new WrapperPlayServerPlayerPositionAndLook(send);
            case BLOCK_CHANGE -> new WrapperPlayServerBlockChange(send);
            case ACKNOWLEDGE_BLOCK_CHANGES -> new WrapperPlayServerAcknowledgeBlockChanges(send);
            case SOUND_EFFECT -> new WrapperPlayServerSoundEffect(send);
            case ENTITY_SOUND_EFFECT -> new WrapperPlayServerEntitySoundEffect(send);
            //case DISCONNECT -> new WrapperPlayServerDisconnect(send);
            case PLAYER_INFO -> new WrapperPlayServerPlayerInfo(send);
            case PLAYER_INFO_UPDATE -> new WrapperPlayServerPlayerInfoUpdate(send);
            case PLAYER_INFO_REMOVE -> new WrapperPlayServerPlayerInfoRemove(send);
            case PLAYER_LIST_HEADER_AND_FOOTER -> new WrapperPlayServerPlayerListHeaderAndFooter(send);
            case PING -> new WrapperPlayServerPing(send);
            case TITLE -> new WrapperPlayServerTitle(send);
            case NBT_QUERY_RESPONSE -> new WrapperPlayServerNBTQueryResponse(send);
            case TAGS -> new WrapperPlayServerTags(send);
            default -> null;
        };
    }

    public Packet(final PacketReceiveEvent receive) {
        this.event = receive;

        this.timestamp = receive.getTimestamp();
        final PacketType.PacketClient type = PacketType.PacketClient.valueOf(receive.getPacketType().getName());
        this.type = type;

        this.wrapper = switch (type) {
            case LOGIN_SUCCESS_ACK -> new WrapperLoginClientLoginSuccessAck(receive);
            case CHAT_PREVIEW -> new WrapperPlayClientChatPreview(receive);
            case TELEPORT_CONFIRM -> new WrapperPlayClientTeleportConfirm(receive);
            case QUERY_BLOCK_NBT -> new WrapperPlayClientQueryBlockNBT(receive);
            case SET_DIFFICULTY -> new WrapperPlayClientSetDifficulty(receive);
            case CHAT_MESSAGE -> new WrapperPlayClientChatMessage(receive);
            case CLIENT_STATUS -> new WrapperPlayClientClientStatus(receive);
            case CLIENT_SETTINGS -> new WrapperPlayClientSettings(receive);
            case TAB_COMPLETE -> new WrapperPlayClientTabComplete(receive);
            case WINDOW_CONFIRMATION -> new WrapperPlayClientWindowConfirmation(receive);
            case CLICK_WINDOW_BUTTON -> new WrapperPlayClientClickWindowButton(receive);
            case CLICK_WINDOW -> new WrapperPlayClientClickWindow(receive);
            case CLOSE_WINDOW -> new WrapperPlayClientCloseWindow(receive);
            case PLUGIN_MESSAGE -> new WrapperPlayClientPluginMessage(receive);
            case EDIT_BOOK -> new WrapperPlayClientEditBook(receive);
            case QUERY_ENTITY_NBT -> new WrapperPlayClientQueryEntityNBT(receive);
            case INTERACT_ENTITY -> new WrapperPlayClientInteractEntity(receive);
            case GENERATE_STRUCTURE -> new WrapperPlayClientGenerateStructure(receive);
            case KEEP_ALIVE -> new WrapperPlayClientKeepAlive(receive);
            case LOCK_DIFFICULTY -> new WrapperPlayClientLockDifficulty(receive);
            case PLAYER_POSITION -> new WrapperPlayClientPlayerPosition(receive);
            case PLAYER_POSITION_AND_ROTATION -> new WrapperPlayClientPlayerPositionAndRotation(receive);
            case PLAYER_ROTATION -> new WrapperPlayClientPlayerRotation(receive);
            case PLAYER_FLYING -> new WrapperPlayClientPlayerFlying(receive);
            case CLIENT_TICK_END -> new WrapperPlayClientClientTickEnd(receive);
            case PLAYER_INPUT -> new WrapperPlayClientPlayerInput(receive);
            case VEHICLE_MOVE -> new WrapperPlayClientVehicleMove(receive);
            case STEER_BOAT -> new WrapperPlayClientSteerBoat(receive);
            case PICK_ITEM -> new WrapperPlayClientPickItem(receive);
            case CRAFT_RECIPE_REQUEST -> new WrapperPlayClientCraftRecipeRequest(receive);
            case PLAYER_ABILITIES -> new WrapperPlayClientPlayerAbilities(receive);
            case PLAYER_DIGGING -> new WrapperPlayClientPlayerDigging(receive);
            case ENTITY_ACTION -> new WrapperPlayClientEntityAction(receive);
            case STEER_VEHICLE -> new WrapperPlayClientSteerVehicle(receive);
            case PONG -> new WrapperPlayClientPong(receive);
            case SET_DISPLAYED_RECIPE -> new WrapperPlayClientSetDisplayedRecipe(receive);
            case SET_RECIPE_BOOK_STATE -> new WrapperPlayClientSetRecipeBookState(receive);
            case NAME_ITEM -> new WrapperPlayClientNameItem(receive);
            case RESOURCE_PACK_STATUS -> new WrapperPlayClientResourcePackStatus(receive);
            case ADVANCEMENT_TAB -> new WrapperPlayClientAdvancementTab(receive);
            case SELECT_TRADE -> new WrapperPlayClientSelectTrade(receive);
            case SET_BEACON_EFFECT -> new WrapperPlayClientSetBeaconEffect(receive);
            case HELD_ITEM_CHANGE -> new WrapperPlayClientHeldItemChange(receive);
            case UPDATE_COMMAND_BLOCK -> new WrapperPlayClientUpdateCommandBlock(receive);
            case UPDATE_COMMAND_BLOCK_MINECART -> new WrapperPlayClientUpdateCommandBlockMinecart(receive);
            case CREATIVE_INVENTORY_ACTION -> new WrapperPlayClientCreativeInventoryAction(receive);
            case UPDATE_JIGSAW_BLOCK -> new WrapperPlayClientUpdateJigsawBlock(receive);
            case UPDATE_SIGN -> new WrapperPlayClientUpdateSign(receive);
            case ANIMATION -> new WrapperPlayClientAnimation(receive);
            case SPECTATE -> new WrapperPlayClientSpectate(receive);
            case PLAYER_BLOCK_PLACEMENT -> new WrapperPlayClientPlayerBlockPlacement(receive);
            case USE_ITEM -> new WrapperPlayClientUseItem(receive);
            case CHAT_COMMAND -> new WrapperPlayClientChatCommand(receive);
            case CHAT_ACK -> new WrapperPlayClientChatAck(receive);
            case CHAT_SESSION_UPDATE -> new WrapperPlayClientChatSessionUpdate(receive);
            case CHUNK_BATCH_ACK -> new WrapperPlayClientChunkBatchAck(receive);
            case CONFIGURATION_ACK -> new WrapperPlayClientConfigurationAck(receive);
            case DEBUG_PING -> new WrapperPlayClientDebugPing(receive);
            case PLAYER_LOADED -> new WrapperPlayClientPlayerLoaded(receive);
            case SLOT_STATE_CHANGE -> new WrapperPlayClientSlotStateChange(receive);
            case CHAT_COMMAND_UNSIGNED -> new WrapperPlayClientChatCommandUnsigned(receive);
            case COOKIE_RESPONSE -> new WrapperPlayClientCookieResponse(receive);
            case SELECT_KNOWN_PACKS -> new WrapperConfigClientSelectKnownPacks(receive);
            case CONFIGURATION_END_ACK -> new WrapperConfigClientConfigurationEndAck(receive);
            case DEBUG_SAMPLE_SUBSCRIPTION -> new WrapperPlayClientDebugSampleSubscription(receive);
            default -> null;
        };
    }

    public boolean isC2S() {
        return this.type instanceof PacketType.PacketClient;
    }

    public boolean isS2C() {
        return this.type instanceof PacketType.PacketServer;
    }

    public boolean isMovement() {
        if (!(type instanceof PacketType.PacketClient client)) return false;
        return (
                client.equals(PacketType.PacketClient.PLAYER_FLYING) ||
                        client.equals(PacketType.PacketClient.PLAYER_POSITION) ||
                        client.equals(PacketType.PacketClient.PLAYER_ROTATION) ||
                        client.equals(PacketType.PacketClient.PLAYER_POSITION_AND_ROTATION));
    }

    public boolean isPosition() {
        if (!(type instanceof PacketType.PacketClient client)) return false;
        return (
                client.equals(PacketType.PacketClient.PLAYER_FLYING) ||
                        client.equals(PacketType.PacketClient.PLAYER_POSITION) ||
                        client.equals(PacketType.PacketClient.PLAYER_POSITION_AND_ROTATION));
    }

    public boolean isRotation() {
        if (!(type instanceof PacketType.PacketClient client)) return false;
        return (
                client.equals(PacketType.PacketClient.PLAYER_FLYING) ||
                        client.equals(PacketType.PacketClient.PLAYER_ROTATION) ||
                        client.equals(PacketType.PacketClient.PLAYER_POSITION_AND_ROTATION));
    }

    public boolean isDebug() {
        return !isRotation() && !isPosition() && (this.type instanceof PacketType.PacketServer serverPacket) && (serverPacket != PacketType.PacketServer.SYSTEM_CHAT_MESSAGE);
    }

    public boolean is(PacketType.PacketClient... type) {
        return Arrays.stream(type).anyMatch(this::is);
    }

    public boolean is(PacketType.PacketServer... type) {
        return Arrays.stream(type).anyMatch(this::is);
    }

    public boolean is(PacketType.PacketClient type) {
        if (!(this.type instanceof PacketType.PacketClient clientPacket)) return false;
        return clientPacket.equals(type);
    }

    public boolean is(PacketType.PacketServer type) {
        if (!(this.type instanceof PacketType.PacketServer clientPacket)) return false;
        return clientPacket.equals(type);
    }

    public void cancel() {
        setCancelled(true);
    }

    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

    public boolean isCancelled() {
        return event.isCancelled();
    }

}