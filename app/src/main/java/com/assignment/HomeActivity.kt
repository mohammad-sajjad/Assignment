package com.assignment

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.assignment.adapter.AudioAdapter
import com.assignment.model.AudioModel
import com.assignment.utils.shortToast
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File


class HomeActivity : AppCompatActivity(), AudioAdapter.OnItemSwipeListener {

    companion object {
        const val extra_key = "home_activity_extra"
    }

    private var extra: HomeExtra? = null
    private var firebaseAuth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    private val audioList = mutableListOf<AudioModel>()

    private var mediaPlayer: MediaPlayer? = null

    private var adapter: AudioAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        init()

        if (checkPermission()) {

            getAudioFiles()

        }
    }

    private fun init() {
        if (intent.getParcelableExtra<HomeExtra>(extra_key) != null) {
            extra = intent.getParcelableExtra(extra_key)
        }
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();

        if (extra != null) {
            home_profile_image.setImageURI(extra?.image)
            home_user_email.text = extra?.email
            home_user_name.text = extra?.userName
        }

        home_logout_btn.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        // Firebase sign out
        firebaseAuth!!.signOut()
        LoginManager.getInstance().logOut()

        // Google sign out
        googleSignInClient!!.signOut().addOnCompleteListener(
            this
        ) { // Google Sign In failed, update UI appropriately
            finish()
            Log.e("login status", "Signed out of google")
        }
    }

    @SuppressLint("Recycle")
    fun getAudioFiles() {
        val contentResolver = contentResolver
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        //looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val title: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val artist: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val duration: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val url =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))

                val modelAudio = AudioModel(title, duration, artist, url)
                audioList.add(modelAudio)

            } while (cursor.moveToNext())
        }

        if (audioList.isEmpty()) {
            shortToast("No files found")
        } else {
            home_audio_recyclerView.layoutManager = LinearLayoutManager(this)
            adapter = AudioAdapter(this, audioList, this)
            home_audio_recyclerView.adapter = adapter


            val simpleCallback: ItemTouchHelper.SimpleCallback =
                object : ItemTouchHelper.SimpleCallback(0, LEFT) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: ViewHolder,
                        target: ViewHolder
                    ): Boolean {
                        Log.d("TAG", "onMove")
                        adapter!!.onItemMove(
                            viewHolder.adapterPosition,
                            target.adapterPosition
                        )
                        return true
                    }

                    override fun getSwipeThreshold(viewHolder: ViewHolder): Float {
                        return 0.5f
                    }

                    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                        val position = viewHolder.adapterPosition
                        if (direction == LEFT) {
                            shortToast("left")
                            Log.d("TAG", " onswipe left")
                        }
                        if(mediaPlayer != null) {
                            if (mediaPlayer?.isPlaying!!) {
                                stopPlaying()
                            }
                            else {
                                playAudio(position)
                            }
                        }
                        else {
                            playAudio(position)
                        }
                        Log.d("TAG", " else onswipe $direction")
                    }

                    override fun getMovementFlags(
                        recyclerView: RecyclerView,
                        viewHolder: ViewHolder
                    ): Int {
                        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                        Log.d("TAG", "getMovementFlags dragFlags $dragFlags swipeFlags $swipeFlags")
                        return makeMovementFlags(dragFlags, swipeFlags)
                    }
                }

            val itemTouchHelper = ItemTouchHelper(simpleCallback)

            itemTouchHelper.attachToRecyclerView(home_audio_recyclerView)

        }
    }

    //runtime storage permission
    private fun checkPermission(): Boolean {
        val READ_EXTERNAL_PERMISSION =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        if (READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_READ
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_READ -> {
                if (grantResults.isNotEmpty() && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(
                            applicationContext,
                            "Please allow storage permission",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        getAudioFiles()
                    }
                }
            }
        }
    }

    override fun playMusic(position: Int) {
        Log.d("TAG", " onItemSwipe $position")
        playAudio(position)
    }

    override fun stopMusic(position: Int) {
        stopPlaying()
    }

    private var startTime = 0
    private var finalTime = 0
    private fun playAudio(pos: Int) {
        try {
            mediaPlayer = MediaPlayer()
            val uri = Uri.fromFile(File(audioList[pos].audioUri!!))
            Log.d("TAG", " playAudio $pos")
            mediaPlayer?.reset()

                if (!mediaPlayer?.isPlaying!!) {
                    mediaPlayer?.apply {
                        setDataSource(applicationContext, uri)
                        prepare()
                        start()
                    }
                    finalTime = mediaPlayer?.duration!!
                    startTime = mediaPlayer!!.currentPosition

                } else {
                    playAudio(pos)
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopPlaying() {
        if (mediaPlayer != null)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onStop() {
        super.onStop()
        stopPlaying()
    }
}