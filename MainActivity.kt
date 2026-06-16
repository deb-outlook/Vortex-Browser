package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.BrowserDatabase
import com.example.data.BrowserRepository
import com.example.ui.BrowserViewModel
import com.example.ui.BrowserViewModelFactory
import com.example.ui.MainAppScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Abstract the Room local database thread bounds
    val database = Room.databaseBuilder(
      applicationContext,
      BrowserDatabase::class.java,
      "aura_browser_database"
    ).fallbackToDestructiveMigration().build()
    
    val repository = BrowserRepository(database.browserDao())
    val viewModel = ViewModelProvider(this, BrowserViewModelFactory(repository))[BrowserViewModel::class.java]

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(dynamicColor = false) { // Preserve our gorgeous dark 3D theme colors
        MainAppScreen(viewModel = viewModel)
      }
    }
  }
}
