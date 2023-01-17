Reloading single tile: https://stackoverflow.com/questions/72015263/update-reload-specific-tile-in-leaflet-dynamicly

mogrify -format png32 -path ../experimental32 *.png

https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames

// assets/minecraft/textures/block/acacia_door_top.png

https://github.com/leonbloy/pngj

### Design choices
`withLock` implementation found in `UpdateQueue` and `MapAreaAccess` - 
These are operations that can be accessed from multiple threads but none of them are crucial do be finished quickly. 
The most important thing here is to not waste any CPU time constantly checking whether the concurrent implementation of some data structure can be used.
That's why I'm using a delay in a suspend function and using regular, non-synchronized implementations of data structures.




### Exceptions
```
 io.netty.util.IllegalReferenceCountException: refCnt: 0, decrement: 1
	at io.netty.util.internal.ReferenceCountUpdater.toLiveRealRefCnt(ReferenceCountUpdater.java:83) ~[netty-common-4.1.82.Final.jar:4.1.82.Final]
	at io.netty.util.internal.ReferenceCountUpdater.release(ReferenceCountUpdater.java:147) ~[netty-common-4.1.82.Final.jar:4.1.82.Final]
	at io.netty.buffer.AbstractReferenceCountedByteBuf.release(AbstractReferenceCountedByteBuf.java:101) ~[netty-buffer-4.1.82.Final.jar:4.1.82.Final]
	at net.minecraft.network.PacketByteBuf.release(PacketByteBuf.java:1738) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.client.network.ClientPlayNetworkHandler.onCustomPayload(ClientPlayNetworkHandler.java:2124) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket.apply(CustomPayloadS2CPacket.java:60) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket.apply(CustomPayloadS2CPacket.java:8) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.network.NetworkThreadUtils.method_11072(NetworkThreadUtils.java:22) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.util.thread.ThreadExecutor.executeTask(ThreadExecutor.java:156) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.util.thread.ReentrantThreadExecutor.executeTask(ReentrantThreadExecutor.java:23) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.util.thread.ThreadExecutor.runTask(ThreadExecutor.java:130) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.util.thread.ThreadExecutor.runTasks(ThreadExecutor.java:115) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.client.MinecraftClient.render(MinecraftClient.java:1144) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.client.MinecraftClient.run(MinecraftClient.java:781) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.client.main.Main.main(Main.java:244) ~[minecraft-project-@-merged-named.jar:?]
	at net.minecraft.client.main.Main.main(Main.java:51) ~[minecraft-project-@-merged-named.jar:?]
	at net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider.launch(MinecraftGameProvider.java:461) ~[fabric-loader-0.14.12.jar:?]
	at net.fabricmc.loader.impl.launch.knot.Knot.launch(Knot.java:74) ~[fabric-loader-0.14.12.jar:?]
	at net.fabricmc.loader.impl.launch.knot.KnotClient.main(KnotClient.java:23) ~[fabric-loader-0.14.12.jar:?]
	at net.fabricmc.devlaunchinjector.Main.main(Main.java:86) ~[dev-launch-injector-0.2.1+build.8.jar:?]
```