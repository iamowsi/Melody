package com.mustafa.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.mustafa.musicplayer.data.MusicRepository
import com.mustafa.musicplayer.data.Song
import com.mustafa.musicplayer.player.PlaybackService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MelodyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MelodyApp()
                }
            }
        }
    }
}

@Composable
private fun MelodyTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MelodyApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showNowPlaying by remember { mutableStateOf(false) }

    // وضعیت پخش‌کننده Media3
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var currentMediaId by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }

    // اتصال به PlaybackService
    DisposableEffect(context) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        var playerListener: Player.Listener? = null

        controllerFuture.addListener(
            {
                runCatching { controllerFuture.get() }.onSuccess { controller ->
                    mediaController = controller
                    isPlaying = controller.isPlaying
                    currentMediaId = controller.currentMediaItem?.mediaId
                    currentPositionMs = controller.currentPosition.coerceAtLeast(0L)
                    durationMs = controller.duration.coerceAtLeast(0L)

                    val listener = object : Player.Listener {
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                        }

                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            currentMediaId = mediaItem?.mediaId
                            currentPositionMs = controller.currentPosition.coerceAtLeast(0L)
                            durationMs = controller.duration.coerceAtLeast(0L)
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            durationMs = controller.duration.coerceAtLeast(0L)
                        }
                    }
                    playerListener = listener
                    controller.addListener(listener)
                }
            },
            ContextCompat.getMainExecutor(context)
        )

        onDispose {
            playerListener?.let { mediaController?.removeListener(it) }
            MediaController.releaseFuture(controllerFuture)
            mediaController = null
        }
    }

    // به‌روزرسانی لحظه‌ای نوار زمان هنگام پخش
    LaunchedEffect(mediaController, isPlaying) {
        while (isPlaying) {
            mediaController?.let { controller ->
                currentPositionMs = controller.currentPosition.coerceAtLeast(0L)
                if (controller.duration > 0L) {
                    durationMs = controller.duration
                }
            }
            delay(500L)
        }
    }

    fun refreshSongs() {
        scope.launch {
            isLoading = true
            songs = MusicRepository.loadLocalSongs(context)
            isLoading = false
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            isLoading = true
            songs = MusicRepository.loadLocalSongs(context)
            isLoading = false
        }
    }

    val filteredSongs = remember(songs, searchQuery) {
        if (searchQuery.isBlank()) {
            songs
        } else {
            val q = searchQuery.trim()
            songs.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.artist.contains(q, ignoreCase = true) ||
                    it.album.contains(q, ignoreCase = true)
            }
        }
    }

    val currentSong = remember(songs, currentMediaId) {
        songs.firstOrNull { it.id.toString() == currentMediaId }
    }

    fun playQueue(queue: List<Song>, startIndex: Int) {
        val controller = mediaController ?: return
        val mediaItems = queue.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.contentUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtUri)
                        .build()
                )
                .build()
        }
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
        currentMediaId = queue[startIndex].id.toString()
        durationMs = queue[startIndex].durationMs
        currentPositionMs = 0L
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) controller.pause() else controller.play()
    }

    if (showNowPlaying && currentSong != null) {
        BackHandler { showNowPlaying = false }
        NowPlayingScreen(
            song = currentSong,
            isPlaying = isPlaying,
            currentPositionMs = currentPositionMs,
            durationMs = if (durationMs > 0L) durationMs else currentSong.durationMs,
            onBack = { showNowPlaying = false },
            onTogglePlayPause = { togglePlayPause() },
            onNext = { mediaController?.seekToNextMediaItem() },
            onPrevious = { mediaController?.seekToPreviousMediaItem() },
            onSeekTo = { position ->
                mediaController?.seekTo(position)
                currentPositionMs = position
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Melody", style = MaterialTheme.typography.titleLarge)
                        if (hasPermission && !isLoading) {
                            Text(
                                text = "${filteredSongs.size} آهنگ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (hasPermission) {
                        IconButton(onClick = { refreshSongs() }) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "بروزرسانی لیست"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (currentSong != null) {
                MiniPlayerBar(
                    song = currentSong,
                    isPlaying = isPlaying,
                    onBarClick = { showNowPlaying = true },
                    onTogglePlayPause = { togglePlayPause() },
                    onNext = { mediaController?.seekToNextMediaItem() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                !hasPermission -> {
                    PermissionRequestView(
                        onRequestClick = { permissionLauncher.launch(audioPermission) }
                    )
                }

                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                songs.isEmpty() -> {
                    EmptyLibraryView(onRefreshClick = { refreshSongs() })
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("جستجوی آهنگ، خواننده یا آلبوم...") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Rounded.Close, contentDescription = "پاک کردن")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            itemsIndexed(
                                items = filteredSongs,
                                key = { _, song -> song.id }
                            ) { index, song ->
                                val isSelected = currentSong?.id == song.id
                                SongRowItem(
                                    song = song,
                                    isSelected = isSelected,
                                    onClick = { playQueue(filteredSongs, index) }
                                )
                                Divider(
                                    modifier = Modifier.padding(start = 80.dp, end = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    onBarClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onBarClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtBox(
                albumArtUri = song.albumArtUri,
                contentDescription = song.album,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "توقف" else "پخش"
                )
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "آهنگ بعدی"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingScreen(
    song: Song,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onBack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }

    val safeDuration = durationMs.coerceAtLeast(1L)
    val sliderValue = if (isDragging) {
        dragPosition
    } else {
        currentPositionMs.coerceIn(0L, safeDuration).toFloat()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("در حال پخش", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            AlbumArtBox(
                albumArtUri = song.albumArtUri,
                contentDescription = song.album,
                iconSize = 84.dp,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = sliderValue,
                    onValueChange = {
                        isDragging = true
                        dragPosition = it
                    },
                    onValueChangeFinished = {
                        onSeekTo(dragPosition.toLong())
                        isDragging = false
                    },
                    valueRange = 0f..safeDuration.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(sliderValue.toLong()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(safeDuration),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "قبلی",
                        modifier = Modifier.size(36.dp)
                    )
                }

                FilledIconButton(
                    onClick = onTogglePlayPause,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "توقف" else "پخش",
                        modifier = Modifier.size(38.dp)
                    )
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "بعدی",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumArtBox(
    albumArtUri: android.net.Uri,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 26.dp
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(iconSize)
        )
        AsyncImage(
            model = albumArtUri,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun SongRowItem(
    song: Song,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.background
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArtBox(
            albumArtUri = song.albumArtUri,
            contentDescription = song.album,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = formatDuration(song.durationMs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionRequestView(onRequestClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.LibraryMusic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "دسترسی به موزیک‌های دستگاه",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "برای پیدا کردن و پخش آهنگ‌های ذخیره شده در گوشی، Melody به مجوز دسترسی فایل‌های صوتی نیاز دارد.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestClick) {
            Text("اجازه دسترسی بده")
        }
    }
}

@Composable
private fun EmptyLibraryView(onRefreshClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "هیچ آهنگی روی دستگاه پیدا نشد",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRefreshClick) {
            Text("اسکن مجدد")
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
