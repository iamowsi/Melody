package com.mustafa.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import com.mustafa.musicplayer.data.MelodyPreferences
import com.mustafa.musicplayer.data.MusicFolder
import com.mustafa.musicplayer.data.MusicRepository
import com.mustafa.musicplayer.data.Song
import com.mustafa.musicplayer.data.ThemeMode
import com.mustafa.musicplayer.player.EqualizerManager
import com.mustafa.musicplayer.player.EqualizerState
import com.mustafa.musicplayer.player.PlaybackService
import com.mustafa.musicplayer.player.SleepTimerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

private val MelodyOledDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B4A),
    onPrimary = Color(0xFF140704),
    primaryContainer = Color(0xFF3E1810),
    onPrimaryContainer = Color(0xFFFFDAD2),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF041D19),
    secondaryContainer = Color(0xFF172226),
    onSecondaryContainer = Color(0xFFA7F3D0),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF5F5F7),
    surface = Color(0xFF0B0B0F),
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF17171F),
    onSurfaceVariant = Color(0xFFA1A1AA),
    outlineVariant = Color(0xFF27272F),
    error = Color(0xFFFF4D6D)
)

private val MelodyLightColorScheme = lightColorScheme(
    primary = Color(0xFFE84A27),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE5DF),
    onPrimaryContainer = Color(0xFF3D0C02),
    secondary = Color(0xFF0F766E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2F4F1),
    onSecondaryContainer = Color(0xFF073B37),
    background = Color(0xFFFAFAFC),
    onBackground = Color(0xFF141418),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF141418),
    surfaceVariant = Color(0xFFF1F1F5),
    onSurfaceVariant = Color(0xFF5F5F6E),
    outlineVariant = Color(0xFFE2E2EA),
    error = Color(0xFFE11D48)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var themeMode by remember { mutableStateOf(MelodyPreferences.loadThemeMode(context)) }

            MelodyTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MelodyApp(
                        themeMode = themeMode,
                        onCycleTheme = {
                            val next = when (themeMode) {
                                ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.SYSTEM
                            }
                            themeMode = next
                            MelodyPreferences.saveThemeMode(context, next)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MelodyTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (useDark) MelodyOledDarkColorScheme else MelodyLightColorScheme
    MaterialTheme(colorScheme = colorScheme, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MelodyApp(
    themeMode: ThemeMode,
    onCycleTheme: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermission = results[audioPermission] == true ||
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
    }

    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var favoriteIds by remember { mutableStateOf(MelodyPreferences.loadFavorites(context)) }
    var folders by remember { mutableStateOf(MelodyPreferences.loadFolders(context)) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var selectedFolderId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showNowPlaying by remember { mutableStateOf(false) }
    var showEqualizer by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var songForFolderDialog by remember { mutableStateOf<Song?>(null) }
    var folderToDelete by remember { mutableStateOf<MusicFolder?>(null) }

    val sleepTimerRemainingMs by SleepTimerManager.remainingMs.collectAsState()
    val equalizerState by EqualizerManager.state.collectAsState()

    // وضعیت پخش‌کننده Media3
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var currentMediaId by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }

    fun toggleFavorite(songId: Long) {
        val updated = if (songId in favoriteIds) favoriteIds - songId else favoriteIds + songId
        favoriteIds = updated
        MelodyPreferences.saveFavorites(context, updated)
    }

    fun createFolder(name: String, initialSongId: Long? = null) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        val newFolder = MusicFolder(
            id = UUID.randomUUID().toString(),
            name = cleanName,
            songIds = if (initialSongId != null) setOf(initialSongId) else emptySet()
        )
        val updated = folders + newFolder
        folders = updated
        MelodyPreferences.saveFolders(context, updated)
    }

    fun deleteFolder(folderId: String) {
        val updated = folders.filterNot { it.id == folderId }
        folders = updated
        if (selectedFolderId == folderId) {
            selectedFolderId = null
        }
        MelodyPreferences.saveFolders(context, updated)
    }

    fun toggleSongInFolder(folderId: String, songId: Long) {
        val updated = folders.map { folder ->
            if (folder.id == folderId) {
                val newIds = if (songId in folder.songIds) folder.songIds - songId else folder.songIds + songId
                folder.copy(songIds = newIds)
            } else {
                folder
            }
        }
        folders = updated
        MelodyPreferences.saveFolders(context, updated)
    }

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
                    shuffleEnabled = controller.shuffleModeEnabled
                    repeatMode = controller.repeatMode
                    currentMediaId = controller.currentMediaItem?.mediaId
                    currentPositionMs = controller.currentPosition.coerceAtLeast(0L)
                    durationMs = controller.duration.coerceAtLeast(0L)

                    val listener = object : Player.Listener {
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                        }

                        override fun onShuffleModeEnabledChanged(enabled: Boolean) {
                            shuffleEnabled = enabled
                        }

                        override fun onRepeatModeChanged(mode: Int) {
                            repeatMode = mode
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
            delay(400L)
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

    val selectedFolder = remember(folders, selectedFolderId) {
        folders.firstOrNull { it.id == selectedFolderId }
    }

    val filteredSongs = remember(songs, searchQuery, showFavoritesOnly, favoriteIds, selectedFolder) {
        val baseList = when {
            showFavoritesOnly -> songs.filter { it.id in favoriteIds }
            selectedFolder != null -> songs.filter { it.id in selectedFolder.songIds }
            else -> songs
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            val q = searchQuery.trim()
            baseList.filter {
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
        if (queue.isEmpty() || startIndex !in queue.indices) return
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

    fun toggleShuffle() {
        val controller = mediaController ?: return
        val next = !controller.shuffleModeEnabled
        controller.shuffleModeEnabled = next
        shuffleEnabled = next
    }

    fun cycleRepeatMode() {
        val controller = mediaController ?: return
        val next = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = next
        repeatMode = next
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            remainingMs = sleepTimerRemainingMs,
            onSelectMinutes = { minutes ->
                SleepTimerManager.start(minutes)
                showSleepTimerDialog = false
            },
            onCancelTimer = {
                SleepTimerManager.cancel()
                showSleepTimerDialog = false
            },
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onCreate = { name ->
                createFolder(name, initialSongId = songForFolderDialog?.id)
                showCreateFolderDialog = false
            },
            onDismiss = { showCreateFolderDialog = false }
        )
    }

    songForFolderDialog?.let { targetSong ->
        AssignFoldersDialog(
            song = targetSong,
            folders = folders,
            onToggleFolder = { folderId -> toggleSongInFolder(folderId, targetSong.id) },
            onCreateNewFolder = { showCreateFolderDialog = true },
            onDismiss = { songForFolderDialog = null }
        )
    }

    folderToDelete?.let { targetFolder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("حذف فولدر") },
            text = {
                Text("آیا فولدر «${targetFolder.name}» حذف شود؟ (فایل‌های اصلی آهنگ‌ها از گوشی پاک نمی‌شوند)")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteFolder(targetFolder.id)
                        folderToDelete = null
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    if (showEqualizer) {
        BackHandler { showEqualizer = false }
        EqualizerScreen(
            state = equalizerState,
            onBack = { showEqualizer = false },
            onToggleEnabled = { EqualizerManager.setEnabled(it) },
            onSelectPreset = { EqualizerManager.usePreset(it) },
            onBandChange = { index, level -> EqualizerManager.setBandLevel(index, level) }
        )
        return
    }

    if (showNowPlaying && currentSong != null) {
        BackHandler { showNowPlaying = false }
        NowPlayingScreen(
            song = currentSong,
            isPlaying = isPlaying,
            isFavorite = currentSong.id in favoriteIds,
            shuffleEnabled = shuffleEnabled,
            repeatMode = repeatMode,
            equalizerEnabled = equalizerState.isEnabled,
            sleepTimerRemainingMs = sleepTimerRemainingMs,
            currentPositionMs = currentPositionMs,
            durationMs = if (durationMs > 0L) durationMs else currentSong.durationMs,
            onBack = { showNowPlaying = false },
            onOpenFolders = { songForFolderDialog = currentSong },
            onOpenEqualizer = { showEqualizer = true },
            onOpenSleepTimer = { showSleepTimerDialog = true },
            onToggleFavorite = { toggleFavorite(currentSong.id) },
            onToggleShuffle = { toggleShuffle() },
            onCycleRepeat = { cycleRepeatMode() },
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Column {
                        Text(
                            text = "Melody",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
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
                    IconButton(onClick = { showEqualizer = true }) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "اکولایزر",
                            tint = if (equalizerState.isEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(onClick = { showSleepTimerDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = "تایمر خواب",
                            tint = if (sleepTimerRemainingMs > 0L) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(onClick = onCycleTheme) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
                                ThemeMode.LIGHT -> Icons.Rounded.LightMode
                                ThemeMode.DARK -> Icons.Rounded.DarkMode
                            },
                            contentDescription = "تغییر تم"
                        )
                    }
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
                        onRequestClick = { permissionLauncher.launch(permissionsToRequest) }
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
                            shape = RoundedCornerShape(18.dp)
                        )

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = !showFavoritesOnly && selectedFolderId == null,
                                    onClick = {
                                        showFavoritesOnly = false
                                        selectedFolderId = null
                                    },
                                    label = { Text("همه (${songs.size})") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = showFavoritesOnly,
                                    onClick = {
                                        showFavoritesOnly = true
                                        selectedFolderId = null
                                    },
                                    label = { Text("علاقه‌مندی‌ها (${favoriteIds.count { id -> songs.any { it.id == id } }})") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Favorite,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                            items(folders, key = { it.id }) { folder ->
                                val count = folder.songIds.count { id -> songs.any { it.id == id } }
                                FilterChip(
                                    selected = selectedFolderId == folder.id,
                                    onClick = {
                                        showFavoritesOnly = false
                                        selectedFolderId = folder.id
                                    },
                                    label = { Text("${folder.name} ($count)") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Folder,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                            item {
                                AssistChip(
                                    onClick = { showCreateFolderDialog = true },
                                    label = { Text("فولدر جدید") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }

                        if (selectedFolder != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "فولدر: ${selectedFolder.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                TextButton(onClick = { folderToDelete = selectedFolder }) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("حذف فولدر", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        if (filteredSongs.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when {
                                        selectedFolder != null && searchQuery.isBlank() ->
                                            "این فولدر هنوز خالی است.\nاز لیست «همه» روی آیکون فولدر کنار هر آهنگ بزن تا به اینجا اضافه شود."
                                        showFavoritesOnly && searchQuery.isBlank() ->
                                            "هنوز آهنگی به علاقه‌مندی‌ها اضافه نکرده‌ای"
                                        else ->
                                            "آهنگی با این مشخصات پیدا نشد"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    bottom = if (currentSong != null) 104.dp else 20.dp
                                )
                            ) {
                                itemsIndexed(
                                    items = filteredSongs,
                                    key = { _, song -> song.id }
                                ) { index, song ->
                                    val isSelected = currentSong?.id == song.id
                                    val isInAnyFolder = folders.any { song.id in it.songIds }
                                    SongRowItem(
                                        song = song,
                                        isSelected = isSelected,
                                        isPlaying = isPlaying,
                                        isFavorite = song.id in favoriteIds,
                                        isInFolder = isInAnyFolder,
                                        onToggleFavorite = { toggleFavorite(song.id) },
                                        onManageFolders = { songForFolderDialog = song },
                                        onClick = { playQueue(filteredSongs, index) }
                                    )
                                    Divider(
                                        modifier = Modifier.padding(start = 80.dp, end = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (currentSong != null) {
                val safeTotal = (if (durationMs > 0L) durationMs else currentSong.durationMs).coerceAtLeast(1L)
                val progress = (currentPositionMs.toFloat() / safeTotal.toFloat()).coerceIn(0f, 1f)
                FloatingMiniPlayerBar(
                    song = currentSong,
                    isPlaying = isPlaying,
                    progress = progress,
                    onBarClick = { showNowPlaying = true },
                    onTogglePlayPause = { togglePlayPause() },
                    onNext = { mediaController?.seekToNextMediaItem() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun FloatingMiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onBarClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        shadowElevation = 14.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onBarClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
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

                FilledIconButton(
                    onClick = onTogglePlayPause,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "توقف" else "پخش",
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "آهنگ بعدی"
                    )
                }
            }

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
private fun PlayingBarsIndicator(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "eq_bars")
    val bar1 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 340),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val heights = if (isPlaying) listOf(bar1, bar2, bar3) else listOf(0.3f, 0.55f, 0.35f)

    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((16f * fraction).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingScreen(
    song: Song,
    isPlaying: Boolean,
    isFavorite: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    equalizerEnabled: Boolean,
    sleepTimerRemainingMs: Long,
    currentPositionMs: Long,
    durationMs: Long,
    onBack: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit
) {
    val context = LocalContext.current
    val fallbackAccent = MaterialTheme.colorScheme.primary
    var extractedColor by remember(song.id) { mutableStateOf(fallbackAccent) }

    LaunchedEffect(song.albumArtUri) {
        extractedColor = extractArtworkColor(context, song.albumArtUri) ?: fallbackAccent
    }

    val animatedGlowColor by animateColorAsState(
        targetValue = extractedColor,
        animationSpec = tween(durationMillis = 650),
        label = "glow_color"
    )

    val artworkScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "artwork_scale"
    )

    val artworkElevation by animateDpAsState(
        targetValue = if (isPlaying) 24.dp else 6.dp,
        animationSpec = tween(durationMillis = 350),
        label = "artwork_elevation"
    )

    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }

    val safeDuration = durationMs.coerceAtLeast(1L)
    val sliderValue = if (isDragging) {
        dragPosition
    } else {
        currentPositionMs.coerceIn(0L, safeDuration).toFloat()
    }

    val baseBg = MaterialTheme.colorScheme.background
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            animatedGlowColor.copy(alpha = 0.55f),
            animatedGlowColor.copy(alpha = 0.22f),
            baseBg
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseBg)
            .background(gradientBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    title = {
                        Column {
                            Text("در حال پخش", style = MaterialTheme.typography.titleMedium)
                            if (sleepTimerRemainingMs > 0L) {
                                Text(
                                    text = "تایمر خواب: ${formatDuration(sleepTimerRemainingMs)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "بازگشت"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenFolders) {
                            Icon(
                                imageVector = Icons.Rounded.CreateNewFolder,
                                contentDescription = "افزودن به فولدر",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onOpenEqualizer) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = "اکولایزر",
                                tint = if (equalizerEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        IconButton(onClick = onOpenSleepTimer) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = "تایمر خواب",
                                tint = if (sleepTimerRemainingMs > 0L) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "علاقه‌مندی",
                                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
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
                Spacer(modifier = Modifier.height(6.dp))

                AlbumArtBox(
                    albumArtUri = song.albumArtUri,
                    contentDescription = song.album,
                    iconSize = 84.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .aspectRatio(1f)
                        .scale(artworkScale)
                        .shadow(
                            elevation = artworkElevation,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = animatedGlowColor,
                            spotColor = animatedGlowColor
                        )
                        .clip(RoundedCornerShape(28.dp))
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "پخش تصادفی",
                            tint = if (shuffleEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "قبلی",
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(12.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "توقف" else "پخش",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "بعدی",
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    IconButton(onClick = onCycleRepeat) {
                        Icon(
                            imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) {
                                Icons.Rounded.RepeatOne
                            } else {
                                Icons.Rounded.Repeat
                            },
                            contentDescription = "تکرار",
                            tint = if (repeatMode != Player.REPEAT_MODE_OFF) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

private suspend fun extractArtworkColor(context: Context, uri: Uri): Color? = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val options = BitmapFactory.Options().apply { inSampleSize = 4 }
            val bitmap = BitmapFactory.decodeStream(stream, null, options) ?: return@withContext null
            try {
                val palette = Palette.from(bitmap).generate()
                val colorInt = palette.vibrantSwatch?.rgb
                    ?: palette.dominantSwatch?.rgb
                    ?: palette.mutedSwatch?.rgb
                    ?: palette.darkVibrantSwatch?.rgb
                colorInt?.let { Color(it) }
            } finally {
                bitmap.recycle()
            }
        }
    }.getOrNull()
}

@Composable
private fun CreateFolderDialog(
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ساخت فولدر جدید") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                placeholder = { Text("مثلاً: رانندگی، ورزش، آرامش‌بخش...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(folderName) },
                enabled = folderName.trim().isNotEmpty()
            ) {
                Text("ساخت فولدر")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
private fun AssignFoldersDialog(
    song: Song,
    folders: List<MusicFolder>,
    onToggleFolder: (String) -> Unit,
    onCreateNewFolder: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("افزودن به فولدر")
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (folders.isEmpty()) {
                    Text(
                        text = "هنوز فولدری نساخته‌ای. اول یک فولدر جدید بساز:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    folders.forEach { folder ->
                        val checked = song.id in folder.songIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleFolder(folder.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { onToggleFolder(folder.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCreateNewFolder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ساخت فولدر جدید")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("تأیید")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EqualizerScreen(
    state: EqualizerState,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (Int) -> Unit,
    onBandChange: (Short, Short) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("اکولایزر", style = MaterialTheme.typography.titleLarge) },
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
        if (!state.isAvailable) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ابتدا یک آهنگ پخش کن",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "به‌محض شروع پخش اولین آهنگ، موتور اکولایزر دستگاه به Melody متصل می‌شود و قابل تنظیم خواهد بود.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "فعال‌سازی اکولایزر",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (state.isEnabled) "روشن" else "خاموش",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = onToggleEnabled
                    )
                }
            }

            if (state.presets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "حالت‌های آماده (Presets)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(state.presets) { index, name ->
                            FilterChip(
                                selected = state.selectedPreset == index && state.isEnabled,
                                onClick = { onSelectPreset(index) },
                                label = { Text(name) }
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "تنظیم دستی باندهای فرکانسی",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val minLevel = state.minBandLevel.toFloat()
                val maxLevel = state.maxBandLevel.toFloat().coerceAtLeast(minLevel + 1f)

                state.bands.forEach { band ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatFrequency(band.centerFreqHz),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = formatDecibels(band.levelMb),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = band.levelMb.toFloat().coerceIn(minLevel, maxLevel),
                                onValueChange = { value ->
                                    onBandChange(band.index, value.roundToInt().toShort())
                                },
                                valueRange = minLevel..maxLevel,
                                enabled = state.isEnabled,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepTimerDialog(
    remainingMs: Long,
    onSelectMinutes: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    val presets = listOf(15, 30, 45, 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تایمر خواب") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (remainingMs > 0L) {
                    Text(
                        text = "زمان باقی‌مانده: ${formatDuration(remainingMs)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "پس از زمان انتخاب‌شده، پخش موزیک به صورت خودکار متوقف می‌شود:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                presets.forEach { minutes ->
                    OutlinedButton(
                        onClick = { onSelectMinutes(minutes) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("$minutes دقیقه")
                    }
                }
            }
        },
        confirmButton = {
            if (remainingMs > 0L) {
                TextButton(onClick = onCancelTimer) {
                    Text("لغو تایمر", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )
}

@Composable
private fun AlbumArtBox(
    albumArtUri: Uri,
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
    isPlaying: Boolean,
    isFavorite: Boolean,
    isInFolder: Boolean,
    onToggleFavorite: () -> Unit,
    onManageFolders: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                else Color.Transparent
            )
            .padding(start = 16.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            AlbumArtBox(
                albumArtUri = song.albumArtUri,
                contentDescription = song.album,
                modifier = Modifier.fillMaxSize()
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    PlayingBarsIndicator(
                        isPlaying = isPlaying,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
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

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = formatDuration(song.durationMs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        IconButton(
            onClick = onManageFolders,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CreateNewFolder,
                contentDescription = "افزودن به فولدر",
                tint = if (isInFolder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = "علاقه‌مندی",
                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
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

private fun formatFrequency(hz: Int): String {
    return if (hz >= 1000) {
        val khz = hz / 1000f
        if (hz % 1000 == 0) "${hz / 1000} kHz"
        else String.format(Locale.US, "%.1f kHz", khz)
    } else {
        "$hz Hz"
    }
}

private fun formatDecibels(levelMb: Short): String {
    val db = levelMb / 100
    return if (db > 0) "+$db dB" else "$db dB"
}
