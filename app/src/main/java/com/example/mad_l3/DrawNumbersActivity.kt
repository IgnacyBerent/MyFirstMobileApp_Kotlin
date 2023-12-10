package com.example.mad_l3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Button
import android.os.Handler
import android.graphics.Color
import android.view.View
import android.os.Looper
import com.example.mad_l3.custom_elements.CustomLottoBallView
import com.example.mad_l3.project_functions.SnackbarHelper.showErrorSnackBar
import com.example.mad_l3.firestore.FireStoreData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class DrawNumbersActivity : AppCompatActivity() {

    private val colors = listOf(
        Color.BLUE,
        Color.CYAN,
        Color.parseColor("#7d2307"), //brown
        Color.GRAY,
        Color.LTGRAY,
        Color.MAGENTA,
        Color.YELLOW,
        Color.parseColor("#2bff9f"), //seagreen
        Color.RED,
        Color.GREEN,
    )
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rootView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootView = findViewById<View>(android.R.id.content)
        setContentView(R.layout.activity_third)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val getNumbersButton = findViewById<Button>(R.id.getnumbers_button)
        // I have created my own custom view - CustomLottoBallView
        val ball1 = findViewById<CustomLottoBallView>(R.id.customCircleView1)
        val ball2 = findViewById<CustomLottoBallView>(R.id.customCircleView2)
        val ball3 = findViewById<CustomLottoBallView>(R.id.customCircleView3)
        val ball4 = findViewById<CustomLottoBallView>(R.id.customCircleView4)
        val ball5 = findViewById<CustomLottoBallView>(R.id.customCircleView5)
        val ball6 = findViewById<CustomLottoBallView>(R.id.customCircleView6)

        val balls = listOf(
            ball1,
            ball2,
            ball3,
            ball4,
            ball5,
            ball6,
        )

        var userNumbers: IntArray? = IntArray(6)

        db.collection("usersNumbers")
            .document(auth.currentUser?.uid.toString())
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val dbData = documentSnapshot.toObject(FireStoreData::class.java)
                    userNumbers = dbData?.selNumb?.toIntArray()
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                println("Error getting document usersNumbers - " +
                        "${FirebaseAuth.getInstance().currentUser?.uid}: $e")
            }


        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.max = 6
        val delayMillis = 1000L
        val handler = Handler(Looper.getMainLooper())

        getNumbersButton.setOnClickListener {

            // make all balls invisible
            for (ball in balls) {
                ball.visibility = View.INVISIBLE
            }

            getNumbersButton.isEnabled = false
            // creates list of numbers from 0 to 49, shuffles it and takes first 6 numbers
            // this way we get 6 random numbers without repetition
            val drawnNumbers = (0..49).shuffled().take(6)
            var matches = 0
            for (number in drawnNumbers) {
                if (userNumbers!!.contains(number)) {
                    matches += 1
                }
            }

            val winningAmount = calculateWinningAmount(matches)
            val winText: String
            val winColor: String
            if (winningAmount > 0) {
                winText = "You won: \n" +
                        " $winningAmount!"
                winColor = "green"
            } else {
                winText = "You lose! Try Again!"
                winColor = "red"
            }

            // sets number and random color for each ball
            for ((i, ball) in balls.withIndex()) {
                ball.setNumber(drawnNumbers[i])
                ball.setCircleColor(colors.random())
            }

            Thread {
                var progressStatus = 0
                while (progressStatus < 6) {
                    progressStatus += 1
                    handler.post {
                        progressBar.progress = progressStatus
                        val id = resources.getIdentifier(
                            "customCircleView$progressStatus",
                            "id",
                            packageName
                        )
                        val ball = findViewById<CustomLottoBallView>(id)
                        ball.visibility = View.VISIBLE
                        // If a ball number matches a guess number,
                        // number on the ball is colored green, otherwise red
                        if (ball.getNumber().toInt() in userNumbers!!) {
                            ball.setTextColor(Color.GREEN)
                        } else {
                            ball.setTextColor(Color.RED)
                        }

                        if (progressStatus == 6) {
                            showErrorSnackBar(rootView, winText, winColor)
                            getNumbersButton.text = "TRY AGAIN!"
                            getNumbersButton.isEnabled = true
                        }
                    }
                    try {
                        Thread.sleep(delayMillis)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    runOnUiThread {
                        val updates = mapOf(
                            "win" to winningAmount,
                            "drawNumb" to drawnNumbers.toList()
                        )

                        db.collection("usersNumbers")
                            .document(auth.currentUser?.uid.toString())
                            .update(updates)
                            .addOnSuccessListener {
                                // Handle success
                                println("Document updated successfully in usersNumbers" +
                                        "/${FirebaseAuth.getInstance().currentUser?.uid}")
                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                println("Error updating document in usersNumbers" +
                                        "/${FirebaseAuth.getInstance().currentUser?.uid}: $e")
                            }
                    }
                }
            }.start()
        }


    }
    private fun calculateWinningAmount(matches: Int): Int {
        return when (matches) {
            3 -> 24
            4 -> 100
            5 -> 10000
            6 -> 1000000
            else -> 0
        }
    }

}