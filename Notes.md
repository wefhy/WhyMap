Reloading single tile: https://stackoverflow.com/questions/72015263/update-reload-specific-tile-in-leaflet-dynamicly

mogrify -format png32 -path ../experimental32 *.png

https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames

// assets/minecraft/textures/block/acacia_door_top.png

https://github.com/leonbloy/pngj





## TODO
 - [ ] add waypoint repository
 - [ ] add waypoint ID, make sure owerwriting wayponits works the way it should
 - [ ] save Byte order in metadata
 - [ ] save biome colors. Loading and displaying worlds should not have any dependency on the game itself.
 - [ ] make migrations tests
 - [ ] more decoupling on all fronts
 - [ ] create encoders and decoders (mostly for tiles but might be required for some other stuff as well)
 - [ ] alert the user if a crash log has been detected. "Can we send it?" "yes"/"no"/"show"
 - [ ] make sure I/O work is really done on I/O dispatcher with infinite threads. Remember that I/O work is blocking so if we run I/O work on a coroutine that uses a dispatcher with 8 threads, one of these 8 threads will be blocked, it's not just a coroutine that will be suspended this way.
   - If all threads are parked and that's the reason the app doesn't do much work, it won't show on the flamechart
 - [ ] either use more of @RequireOptIn as a form of encapsulation or actually make multiple modules in the project
 - [ ] NIO RandomAccessFile might be of use for some of the data manipulations I do
 - 


## Tiles management
 - [ ] loaders should use sharedFlows or some other observable type to broadcast the information
 - So it means tiles should be subscribed to. They can be returned more than once. 
   - If the tile is not loaded, it should be loaded then returned
   - If the tile is loaded, it should be returned instantly, update should be scheduled and then again an updated version should be returned
   - Tiles can be returned again whenever they're updated
 - [ ] in order to get loaded tile, sent a request to loader and then observe the result
 - [ ] there could be alternative way to load the tile using a suspend method if it's only required once. Then it after loading the loader should call the continuation and unsubscribe to the tile
 - [ ] event handling 
   - the frontend requests to load the data but needs to subscribe only to the tiles that it has requested to load or keep loaded
   - after receiving the event that the tile is loaded, the updater can now update the tile but

 - 






















### Design choices
`withLock` implementation found in `UpdateQueue` and `MapAreaAccess` - 
These are operations that can be accessed from multiple threads but none of them are crucial do be finished quickly. 
The most important thing here is to not waste any CPU time constantly checking whether the concurrent implementation of some data structure can be used.
That's why I'm using a delay in a suspend function and using regular, non-synchronized implementations of data structures.



spruce ui
cotton mc
adventure
libgui

Just subclass Screen and call MinecraftClient#setScreen to open it



### Exceptions
#### ex1
```stacktrace
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

#### ex2
```stacktrace 2
[19:37:54] [DefaultDispatcher-worker-11/ERROR] (application) 200 OK: GET - /tiles/17/65527/65537
 java.lang.IndexOutOfBoundsException: null
	at javax.imageio.stream.FileCacheImageOutputStream.seek(FileCacheImageOutputStream.java:170) ~[?:?]
	at com.sun.imageio.plugins.png.IDATOutputStream.finishChunk(PNGImageWriter.java:204) ~[?:?]
	at com.sun.imageio.plugins.png.IDATOutputStream.deflate(PNGImageWriter.java:252) ~[?:?]
	at com.sun.imageio.plugins.png.IDATOutputStream.finish(PNGImageWriter.java:277) ~[?:?]
	at com.sun.imageio.plugins.png.PNGImageWriter.write_IDAT(PNGImageWriter.java:1053) ~[?:?]
	at com.sun.imageio.plugins.png.PNGImageWriter.write(PNGImageWriter.java:1286) ~[?:?]
	at dev.wefhy.whymap.utils.ImageWriter.encodePNG(ImageWriter.kt:50) ~[main/:?]
	at dev.wefhy.whymap.WhyServer$serverRouting$8$1$1.invokeSuspend(WhyServer.kt:185) ~[main/:?]
	at dev.wefhy.whymap.WhyServer$serverRouting$8$1$1.invoke(WhyServer.kt) ~[main/:?]
	at dev.wefhy.whymap.WhyServer$serverRouting$8$1$1.invoke(WhyServer.kt) ~[main/:?]
	at kotlinx.coroutines.intrinsics.UndispatchedKt.startUndispatchedOrReturn(Undispatched.kt:89) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.BuildersKt__Builders_commonKt.withContext(Builders.common.kt:169) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.BuildersKt.withContext(Unknown Source) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at dev.wefhy.whymap.WhyServer$serverRouting$8$1.invokeSuspend(WhyServer.kt:184) ~[main/:?]
	at dev.wefhy.whymap.WhyServer$serverRouting$8$1.invoke(WhyServer.kt) ~[main/:?]
	at dev.wefhy.whymap.WhyServer$serverRouting$8$1.invoke(WhyServer.kt) ~[main/:?]
	at io.ktor.http.content.OutputStreamContent$writeTo$2.invokeSuspend(OutputStreamContent.kt:28) ~[ktor-http-jvm-2.2.2.jar:?]
	at io.ktor.http.content.OutputStreamContent$writeTo$2.invoke(OutputStreamContent.kt) ~[ktor-http-jvm-2.2.2.jar:?]
	at io.ktor.http.content.OutputStreamContent$writeTo$2.invoke(OutputStreamContent.kt) ~[ktor-http-jvm-2.2.2.jar:?]
	at io.ktor.http.content.BlockingBridgeKt.withBlocking(BlockingBridge.kt:28) ~[ktor-http-jvm-2.2.2.jar:?]
	at io.ktor.http.content.OutputStreamContent.writeTo(OutputStreamContent.kt:24) ~[ktor-http-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationResponse$respondWriteChannelContent$2$1.invokeSuspend(BaseApplicationResponse.kt:174) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationResponse$respondWriteChannelContent$2$1.invoke(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationResponse$respondWriteChannelContent$2$1.invoke(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at kotlinx.coroutines.intrinsics.UndispatchedKt.startUndispatchedOrReturn(Undispatched.kt:89) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.BuildersKt__Builders_commonKt.withContext(Builders.common.kt:169) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.BuildersKt.withContext(Unknown Source) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at io.ktor.server.engine.BaseApplicationResponse.respondWriteChannelContent$suspendImpl(BaseApplicationResponse.kt:173) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationResponse.respondWriteChannelContent(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationResponse.respondOutgoingContent$suspendImpl(BaseApplicationResponse.kt:132) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationResponse.respondOutgoingContent(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.cio.CIOApplicationResponse.respondOutgoingContent(CIOApplicationResponse.kt:118) ~[ktor-server-cio-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationResponse$Companion$setupSendPipeline$1.invokeSuspend(BaseApplicationResponse.kt:317) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationResponse$Companion$setupSendPipeline$1.invoke(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationResponse$Companion$setupSendPipeline$1.invoke(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.proceedWith(SuspendFunctionGun.kt:88) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.DefaultTransformKt$installDefaultTransformations$1.invokeSuspend(DefaultTransform.kt:29) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.DefaultTransformKt$installDefaultTransformations$1.invoke(DefaultTransform.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.DefaultTransformKt$installDefaultTransformations$1.invoke(DefaultTransform.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.execute$ktor_utils(SuspendFunctionGun.kt:98) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.Pipeline.execute(Pipeline.kt:77) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.server.response.ApplicationResponseFunctionsJvmKt.respondOutputStream(ApplicationResponseFunctionsJvm.kt:108) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.server.response.ApplicationResponseFunctionsJvmKt.respondOutputStream$default(ApplicationResponseFunctionsJvm.kt:34) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at dev.wefhy.whymap.WhyServer$serverRouting$8.invokeSuspend(WhyServer.kt:183) ~[main/:?]
	at dev.wefhy.whymap.WhyServer$serverRouting$8.invoke(WhyServer.kt) ~[main/:?]
	at dev.wefhy.whymap.WhyServer$serverRouting$8.invoke(WhyServer.kt) ~[main/:?]
	at io.ktor.server.routing.Route$buildPipeline$1$1.invokeSuspend(Route.kt:116) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.server.routing.Route$buildPipeline$1$1.invoke(Route.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.server.routing.Route$buildPipeline$1$1.invoke(Route.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.execute$ktor_utils(SuspendFunctionGun.kt:98) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.Pipeline.execute(Pipeline.kt:77) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.server.routing.Routing$executeResult$$inlined$execute$1.invokeSuspend(Pipeline.kt:478) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.server.routing.Routing$executeResult$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.server.routing.Routing$executeResult$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.util.debug.ContextUtilsKt.initContextInDebugMode(ContextUtils.kt:17) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.server.routing.Routing.executeResult(Routing.kt:190) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.server.routing.Routing.interceptor(Routing.kt:64) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.server.routing.Routing$Plugin$install$1.invokeSuspend(Routing.kt:140) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.server.routing.Routing$Plugin$install$1.invoke(Routing.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.server.routing.Routing$Plugin$install$1.invoke(Routing.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationEngineKt$installDefaultTransformationChecker$1.invokeSuspend(BaseApplicationEngine.kt:123) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationEngineKt$installDefaultTransformationChecker$1.invoke(BaseApplicationEngine.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.BaseApplicationEngineKt$installDefaultTransformationChecker$1.invoke(BaseApplicationEngine.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.execute$ktor_utils(SuspendFunctionGun.kt:98) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.Pipeline.execute(Pipeline.kt:77) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1$invokeSuspend$$inlined$execute$1.invokeSuspend(Pipeline.kt:478) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1$invokeSuspend$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1$invokeSuspend$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.util.debug.ContextUtilsKt.initContextInDebugMode(ContextUtils.kt:17) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1.invokeSuspend(DefaultEnginePipeline.kt:118) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1.invoke(DefaultEnginePipeline.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1.invoke(DefaultEnginePipeline.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.SuspendFunctionGun.execute$ktor_utils(SuspendFunctionGun.kt:98) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.util.pipeline.Pipeline.execute(Pipeline.kt:77) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.server.cio.CIOApplicationEngine$handleRequest$2$invokeSuspend$$inlined$execute$1.invokeSuspend(Pipeline.kt:478) ~[ktor-server-cio-jvm-2.2.2.jar:?]
	at io.ktor.server.cio.CIOApplicationEngine$handleRequest$2$invokeSuspend$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-cio-jvm-2.2.2.jar:?]
	at io.ktor.server.cio.CIOApplicationEngine$handleRequest$2$invokeSuspend$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-cio-jvm-2.2.2.jar:?]
	at io.ktor.util.debug.ContextUtilsKt.initContextInDebugMode(ContextUtils.kt:17) ~[ktor-utils-jvm-2.2.2.jar:?]
	at io.ktor.server.cio.CIOApplicationEngine$handleRequest$2.invokeSuspend(CIOApplicationEngine.kt:191) ~[ktor-server-cio-jvm-2.2.2.jar:?]
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33) ~[kotlin-stdlib-1.8.0.jar:?]
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.internal.LimitedDispatcher.run(LimitedDispatcher.kt:42) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:95) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:570) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:750) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:677) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:664) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	Suppressed: java.io.IOException: kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@64a8b32e
		at io.ktor.utils.io.jvm.javaio.OutputAdapter.close(Blocking.kt:134) ~[ktor-io-jvm-2.2.2.jar:?]
		at kotlin.io.CloseableKt.closeFinally(Closeable.kt:59) ~[kotlin-stdlib-1.8.0.jar:?]
		at io.ktor.http.content.OutputStreamContent$writeTo$2.invokeSuspend(OutputStreamContent.kt:27) ~[ktor-http-jvm-2.2.2.jar:?]
		at io.ktor.http.content.OutputStreamContent$writeTo$2.invoke(OutputStreamContent.kt) ~[ktor-http-jvm-2.2.2.jar:?]
		at io.ktor.http.content.OutputStreamContent$writeTo$2.invoke(OutputStreamContent.kt) ~[ktor-http-jvm-2.2.2.jar:?]
		at io.ktor.http.content.BlockingBridgeKt.withBlocking(BlockingBridge.kt:28) ~[ktor-http-jvm-2.2.2.jar:?]
		at io.ktor.http.content.OutputStreamContent.writeTo(OutputStreamContent.kt:24) ~[ktor-http-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationResponse$respondWriteChannelContent$2$1.invokeSuspend(BaseApplicationResponse.kt:174) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationResponse$respondWriteChannelContent$2$1.invoke(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationResponse$respondWriteChannelContent$2$1.invoke(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at kotlinx.coroutines.intrinsics.UndispatchedKt.startUndispatchedOrReturn(Undispatched.kt:89) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
		at kotlinx.coroutines.BuildersKt__Builders_commonKt.withContext(Builders.common.kt:169) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
		at kotlinx.coroutines.BuildersKt.withContext(Unknown Source) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
		at io.ktor.server.engine.BaseApplicationResponse.respondWriteChannelContent$suspendImpl(BaseApplicationResponse.kt:173) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationResponse.respondWriteChannelContent(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationResponse.respondOutgoingContent$suspendImpl(BaseApplicationResponse.kt:132) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationResponse.respondOutgoingContent(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.cio.CIOApplicationResponse.respondOutgoingContent(CIOApplicationResponse.kt:118) ~[ktor-server-cio-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationResponse$Companion$setupSendPipeline$1.invokeSuspend(BaseApplicationResponse.kt:317) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationResponse$Companion$setupSendPipeline$1.invoke(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationResponse$Companion$setupSendPipeline$1.invoke(BaseApplicationResponse.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.proceedWith(SuspendFunctionGun.kt:88) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.DefaultTransformKt$installDefaultTransformations$1.invokeSuspend(DefaultTransform.kt:29) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.DefaultTransformKt$installDefaultTransformations$1.invoke(DefaultTransform.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.DefaultTransformKt$installDefaultTransformations$1.invoke(DefaultTransform.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.execute$ktor_utils(SuspendFunctionGun.kt:98) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.Pipeline.execute(Pipeline.kt:77) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.server.response.ApplicationResponseFunctionsJvmKt.respondOutputStream(ApplicationResponseFunctionsJvm.kt:108) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.server.response.ApplicationResponseFunctionsJvmKt.respondOutputStream$default(ApplicationResponseFunctionsJvm.kt:34) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at dev.wefhy.whymap.WhyServer$serverRouting$8.invokeSuspend(WhyServer.kt:183) ~[main/:?]
		at dev.wefhy.whymap.WhyServer$serverRouting$8.invoke(WhyServer.kt) ~[main/:?]
		at dev.wefhy.whymap.WhyServer$serverRouting$8.invoke(WhyServer.kt) ~[main/:?]
		at io.ktor.server.routing.Route$buildPipeline$1$1.invokeSuspend(Route.kt:116) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.server.routing.Route$buildPipeline$1$1.invoke(Route.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.server.routing.Route$buildPipeline$1$1.invoke(Route.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.execute$ktor_utils(SuspendFunctionGun.kt:98) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.Pipeline.execute(Pipeline.kt:77) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.server.routing.Routing$executeResult$$inlined$execute$1.invokeSuspend(Pipeline.kt:478) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.server.routing.Routing$executeResult$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.server.routing.Routing$executeResult$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.util.debug.ContextUtilsKt.initContextInDebugMode(ContextUtils.kt:17) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.server.routing.Routing.executeResult(Routing.kt:190) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.server.routing.Routing.interceptor(Routing.kt:64) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.server.routing.Routing$Plugin$install$1.invokeSuspend(Routing.kt:140) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.server.routing.Routing$Plugin$install$1.invoke(Routing.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.server.routing.Routing$Plugin$install$1.invoke(Routing.kt) ~[ktor-server-core-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationEngineKt$installDefaultTransformationChecker$1.invokeSuspend(BaseApplicationEngine.kt:123) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationEngineKt$installDefaultTransformationChecker$1.invoke(BaseApplicationEngine.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.BaseApplicationEngineKt$installDefaultTransformationChecker$1.invoke(BaseApplicationEngine.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.execute$ktor_utils(SuspendFunctionGun.kt:98) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.Pipeline.execute(Pipeline.kt:77) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1$invokeSuspend$$inlined$execute$1.invokeSuspend(Pipeline.kt:478) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1$invokeSuspend$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1$invokeSuspend$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.util.debug.ContextUtilsKt.initContextInDebugMode(ContextUtils.kt:17) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1.invokeSuspend(DefaultEnginePipeline.kt:118) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1.invoke(DefaultEnginePipeline.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$1.invoke(DefaultEnginePipeline.kt) ~[ktor-server-host-common-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.loop(SuspendFunctionGun.kt:120) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.proceed(SuspendFunctionGun.kt:78) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.SuspendFunctionGun.execute$ktor_utils(SuspendFunctionGun.kt:98) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.util.pipeline.Pipeline.execute(Pipeline.kt:77) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.server.cio.CIOApplicationEngine$handleRequest$2$invokeSuspend$$inlined$execute$1.invokeSuspend(Pipeline.kt:478) ~[ktor-server-cio-jvm-2.2.2.jar:?]
		at io.ktor.server.cio.CIOApplicationEngine$handleRequest$2$invokeSuspend$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-cio-jvm-2.2.2.jar:?]
		at io.ktor.server.cio.CIOApplicationEngine$handleRequest$2$invokeSuspend$$inlined$execute$1.invoke(Pipeline.kt) ~[ktor-server-cio-jvm-2.2.2.jar:?]
		at io.ktor.util.debug.ContextUtilsKt.initContextInDebugMode(ContextUtils.kt:17) ~[ktor-utils-jvm-2.2.2.jar:?]
		at io.ktor.server.cio.CIOApplicationEngine$handleRequest$2.invokeSuspend(CIOApplicationEngine.kt:191) ~[ktor-server-cio-jvm-2.2.2.jar:?]
		at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33) ~[kotlin-stdlib-1.8.0.jar:?]
		at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
		at kotlinx.coroutines.internal.LimitedDispatcher.run(LimitedDispatcher.kt:42) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
		at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:95) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
		at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:570) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
		at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:750) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
		at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:677) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
		at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:664) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	Caused by: kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled
```