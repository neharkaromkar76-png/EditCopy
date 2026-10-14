package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.EditCopyDatabase
import com.example.data.model.AppScreen
import com.example.data.model.SourceVideoMeta
import com.example.data.model.VideoTemplate
import com.example.data.repository.TemplateRepository
import com.example.ui.components.EditCopyBottomNavBar
import com.example.ui.screens.ApplyWorkflowScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImportProjectScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TemplateDetailsScreen
import com.example.ui.screens.TemplateLibraryScreen
import com.example.ui.screens.TimelineEditorScreen
import com.example.ui.screens.VideoPickerScreen
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.EditCopyTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: TemplateRepository) : ViewModel() {

    val allTemplates: StateFlow<List<VideoTemplate>> = repository.allTemplates
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TemplateRepository.DEFAULT_TEMPLATES
        )

    fun toggleFavorite(id: String, currentFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, currentFavorite)
        }
    }

    fun duplicateTemplate(template: VideoTemplate, onDone: (VideoTemplate) -> Unit = {}) {
        viewModelScope.launch {
            val copy = repository.duplicateTemplate(template)
            onDone(copy)
        }
    }

    fun saveTemplate(template: VideoTemplate) {
        viewModelScope.launch {
            repository.saveTemplate(template)
        }
    }

    fun renameTemplate(id: String, newTitle: String) {
        viewModelScope.launch {
            repository.renameTemplate(id, newTitle)
        }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch {
            repository.deleteTemplate(id)
        }
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = EditCopyDatabase.getDatabase(this)
        val repository = TemplateRepository(database.templateDao())

        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository) as T
            }
        })[MainViewModel::class.java]

        setContent {
            EditCopyTheme {
                EditCopyApp(
                    repository = repository,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun EditCopyApp(
    repository: TemplateRepository,
    viewModel: MainViewModel
) {
    val templates by viewModel.allTemplates.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigation backstack
    val backStack = remember { mutableStateListOf<AppScreen>(AppScreen.Splash) }
    val currentScreen = backStack.lastOrNull() ?: AppScreen.Home

    fun navigateTo(screen: AppScreen, replaceTop: Boolean = false) {
        if (replaceTop && backStack.isNotEmpty()) {
            backStack.removeAt(backStack.lastIndex)
        }
        backStack.add(screen)
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    // Native Android Back button handler
    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }

    val isBottomBarVisible = when (currentScreen) {
        is AppScreen.Home,
        is AppScreen.TemplateLibrary,
        is AppScreen.Library,
        is AppScreen.ProfileSettings -> true
        else -> false
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        containerColor = DeepBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isBottomBarVisible) {
                EditCopyBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { target ->
                        if (target != currentScreen) {
                            navigateTo(target)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isBottomBarVisible) innerPadding else androidx.compose.foundation.layout.PaddingValues())
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_navigation"
            ) { screen ->
                when (screen) {
                    is AppScreen.Splash -> {
                        SplashScreen(
                            onTimeout = {
                                navigateTo(AppScreen.Onboarding, replaceTop = true)
                            }
                        )
                    }

                    is AppScreen.Onboarding -> {
                        OnboardingScreen(
                            onFinish = {
                                navigateTo(AppScreen.Home, replaceTop = true)
                            }
                        )
                    }

                    is AppScreen.Home -> {
                        HomeScreen(
                            templates = templates,
                            onNavigate = { navigateTo(it) },
                            onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) }
                        )
                    }

                    is AppScreen.TemplateLibrary -> {
                        TemplateLibraryScreen(
                            templates = templates,
                            onSelectTemplate = { tplId ->
                                navigateTo(AppScreen.TemplateDetails(tplId))
                            },
                            onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) },
                            onCreateNewTemplate = {
                                navigateTo(AppScreen.TimelineEditor(null))
                            }
                        )
                    }

                    is AppScreen.TemplateDetails -> {
                        val template = templates.find { it.id == screen.templateId }
                            ?: templates.firstOrNull()
                        if (template != null) {
                            TemplateDetailsScreen(
                                template = template,
                                onBack = { navigateBack() },
                                onApplyToVideo = { tplId ->
                                    navigateTo(AppScreen.VideoPicker(targetTemplateId = tplId))
                                },
                                onDuplicate = { tpl ->
                                    viewModel.duplicateTemplate(tpl) { copy ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Template duplicated: ${copy.title}")
                                        }
                                        navigateTo(AppScreen.TemplateDetails(copy.id), replaceTop = true)
                                    }
                                },
                                onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) }
                            )
                        } else {
                            navigateBack()
                        }
                    }

                    is AppScreen.ImportProject -> {
                        ImportProjectScreen(
                            repository = repository,
                            onBack = { navigateBack() },
                            onTemplateImported = { imported ->
                                viewModel.saveTemplate(imported)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Blueprint added to library: ${imported.title}")
                                }
                                navigateTo(AppScreen.TemplateDetails(imported.id), replaceTop = true)
                            }
                        )
                    }

                    is AppScreen.VideoPicker -> {
                        VideoPickerScreen(
                            repository = repository,
                            onBack = { navigateBack() },
                            onVideoSelected = { pickedVideo ->
                                val targetTplId = screen.targetTemplateId ?: templates.first().id
                                navigateTo(
                                    AppScreen.ApplyWorkflow(
                                        templateId = targetTplId,
                                        initialStep = 3,
                                        sourceVideo = pickedVideo
                                    ),
                                    replaceTop = true
                                )
                            }
                        )
                    }

                    is AppScreen.ApplyWorkflow -> {
                        val template = templates.find { it.id == screen.templateId }
                            ?: templates.first()
                        ApplyWorkflowScreen(
                            initialTemplate = template,
                            availableTemplates = templates,
                            repository = repository,
                            initialVideo = screen.sourceVideo,
                            onBack = { navigateBack() },
                            onComplete = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Applied ${template.title} successfully!")
                                }
                                navigateTo(AppScreen.Library, replaceTop = true)
                            }
                        )
                    }

                    is AppScreen.TimelineEditor -> {
                        val existing = screen.templateId?.let { id -> templates.find { it.id == id } }
                        TimelineEditorScreen(
                            templateId = screen.templateId,
                            existingTemplate = existing,
                            onBack = { navigateBack() },
                            onSaveTemplate = { newTpl ->
                                viewModel.saveTemplate(newTpl)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Saved template: ${newTpl.title}")
                                }
                                navigateTo(AppScreen.Library, replaceTop = true)
                            }
                        )
                    }

                    is AppScreen.Library -> {
                        LibraryScreen(
                            templates = templates,
                            onSelectTemplate = { tplId ->
                                navigateTo(AppScreen.TemplateDetails(tplId))
                            },
                            onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) },
                            onRenameTemplate = { id, newName -> viewModel.renameTemplate(id, newName) },
                            onDuplicateTemplate = { tpl ->
                                viewModel.duplicateTemplate(tpl) { copy ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Duplicated: ${copy.title}")
                                    }
                                }
                            },
                            onDeleteTemplate = { id ->
                                viewModel.deleteTemplate(id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Template deleted")
                                }
                            },
                            onCreateNewTemplate = {
                                navigateTo(AppScreen.TimelineEditor(null))
                            }
                        )
                    }

                    is AppScreen.ProfileSettings -> {
                        ProfileSettingsScreen(
                            templatesCount = templates.size,
                            onNavigate = { navigateTo(it) }
                        )
                    }
                }
            }
        }
    }
}
