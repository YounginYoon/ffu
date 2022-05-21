package com.example.ffu.profile

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ffu.BackgroundActivity
import com.example.ffu.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.*
import java.net.Socket
import java.text.SimpleDateFormat

class ProfileSettingActivity : AppCompatActivity() {

    private lateinit var userDB: DatabaseReference
    private lateinit var auth : FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private val ip = "59.9.212.155"
    private val port = 30000
    private lateinit var socket: Socket
    private lateinit var dos: DataOutputStream

    private var initFlag = false
    private val personalities = ArrayList<String>()
    private val hobbies = ArrayList<String>()
    private var mbti: String = ""
    private var religion: String = ""
    private var birth: String = ""
    private var gender: String = ""
    private var personFlag : Boolean = false
    private var requestFlag : Boolean = false

    private lateinit var nickname : String
    private lateinit var job : String
    private lateinit var introMe : String
    private lateinit var smoke : String
    private lateinit var drinking : String
    private lateinit var progressBar : ProgressBar

    private lateinit var editTextArray : Array<EditText>
    private lateinit var radioButtonArray : Array<RadioButton>
    private lateinit var buttonArray : Array<Button>

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_setting)

        initializeInformation()
        setEditableArray()
        setMbti()
        setPersonality()
        setHobby()
        setReligion()
        setPhoto()
        saveProfile()
        requestListener() // request Listener 등록
    }

    private fun initializeInformation() {
        val animation = mutableMapOf<String, Any>()

        auth = Firebase.auth
        storage = FirebaseStorage.getInstance()
        progressBar = findViewById<ProgressBar>(R.id.profile_setting_progressBar)
        if (intent.hasExtra("birth")) {
            birth = intent.getStringExtra("birth").toString()
            gender = intent.getStringExtra("gender").toString()
        }
        userDB = Firebase.database.reference.child("profile").child(auth.uid.toString())
        userDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!initFlag) {
                    initFlag = true
                    for (ds in snapshot.children) {
                        when (ds.key.toString()) {
                            "nickname" -> findViewById<EditText>(R.id.profile_setting_inputNickname).setText(ds.value.toString())
                            "job" -> findViewById<EditText>(R.id.profile_setting_inputJob).setText(ds.value.toString())
                            "introMe" -> findViewById<EditText>(R.id.profile_setting_inputIntroduce).setText(ds.value.toString())
                            "smoke" -> when(ds.value.toString()) {
                                "비흡연" -> findViewById<RadioButton>(R.id.profile_setting_smoke_radioButton1).isChecked = true
                                "흡연" -> findViewById<RadioButton>(R.id.profile_setting_smoke_radioButton2).isChecked = true
                                "가끔" -> findViewById<RadioButton>(R.id.profile_setting_smoke_radioButton3).isChecked = true
                            }
                            "drinking" -> when(ds.value.toString()) {
                                "안함" -> findViewById<RadioButton>(R.id.profile_setting_drink_radioButton1).isChecked = true
                                "자주" -> findViewById<RadioButton>(R.id.profile_setting_drink_radioButton2).isChecked = true
                                "가끔" -> findViewById<RadioButton>(R.id.profile_setting_drink_radioButton3).isChecked = true
                            }
                            "mbti" -> mbti = ds.value.toString()
                            "religion" -> religion = ds.value.toString()
                            "personality" -> {
                                val personalityList = ds.value.toString().split("/")
                                for (personality in personalityList) {
                                    personalities.add(personality)
                                }
                            }
                            "hobby" -> {
                                val hobbyList = ds.value.toString().split("/")
                                for (hobby in hobbyList) {
                                    hobbies.add(hobby)
                                }
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        userDB = Firebase.database.reference.child("animation").child(auth.uid.toString())
        animation["permission"] = "false"
        userDB.updateChildren(animation)
    }

    private fun setEditableArray() {
        editTextArray = arrayOf(findViewById<EditText>(R.id.profile_setting_inputNickname),
            findViewById<EditText>(R.id.profile_setting_inputJob),
            findViewById<EditText>(R.id.profile_setting_inputIntroduce),
            findViewById<EditText>(R.id.profile_setting_inputNickname))
        radioButtonArray = arrayOf(findViewById<RadioButton>(R.id.profile_setting_smoke_radioButton1),
            findViewById<RadioButton>(R.id.profile_setting_smoke_radioButton2),
            findViewById<RadioButton>(R.id.profile_setting_smoke_radioButton3),
            findViewById<RadioButton>(R.id.profile_setting_drink_radioButton1),
            findViewById<RadioButton>(R.id.profile_setting_drink_radioButton2),
            findViewById<RadioButton>(R.id.profile_setting_drink_radioButton3))
        buttonArray = arrayOf(findViewById<Button>(R.id.profile_setting_mbtiButton),
            findViewById<Button>(R.id.profile_setting_personalButton),
            findViewById<Button>(R.id.profile_setting_religionButton),
            findViewById<Button>(R.id.profile_setting_hobbyButton),
            findViewById<Button>(R.id.profile_setting_saveButton))
    }

    private fun setMbti() {
        val mbtiButton = findViewById<Button>(R.id.profile_setting_mbtiButton)

        mbtiButton.setOnClickListener {
            val items = arrayOf("ESTJ", "ESFJ", "ENFJ", "ENTJ",
                "ENTP", "ENFP", "ESFP", "ESTP",
                "INTP", "INFP", "ISFP", "ISTP",
                "ISTJ", "ISFJ", "INFJ", "INTJ")
            var checkedItem = -1
            var i = 0

            for (item in items) {
                if (mbti.equals(item)) {
                   checkedItem = i
                   break
                }
                i++
            }

            val builder = AlertDialog.Builder(this)
                .setTitle("자신의 MBTI를 하나 선택해주세요")
                .setSingleChoiceItems(items, checkedItem) { dialog, which ->
                    mbti = items[which]
                }
                .setPositiveButton("선택") {dialog, which -> }
                .show()
        }
    }

    private fun setPersonality() {
        val personalityButton = findViewById<Button>(R.id.profile_setting_personalButton)

        personalityButton.setOnClickListener {
            val items = arrayOf("활발한", "조용한", "엉뚱한", "진지한",
                "자유로운", "즉흥적인", "꼼꼼한", "감성적인", "성실한",
                "논리적인", "침착한", "자신감이 넘치는", "애교가 넘치는")
            val checkedItems = booleanArrayOf(false, false, false, false,
                false, false, false, false, false,
                false, false, false, false)
            val selectedItemIndex = ArrayList<Int>()

            for (personality in personalities) {
                var i = 0
                for (item in items) {
                    if (personality.equals(item)) {
                        checkedItems[i] = true
                        selectedItemIndex.add(i)
                        break
                    }
                    i++
                }
            }
            val builder = AlertDialog.Builder(this)
                .setTitle("자신의 성격을 여러개 선택해주세요")
                .setMultiChoiceItems(items, checkedItems){ dialogInterface: DialogInterface, i: Int, b: Boolean ->
                    if(b){
                        checkedItems[i] = true
                        selectedItemIndex.add(i)
                    } else if(selectedItemIndex.contains(i)){
                        checkedItems[i] = false
                        selectedItemIndex.remove(i)
                    }
                }.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                    personalities.clear()
                    for(j in selectedItemIndex) {
                        personalities.add(items[j])
                    }
                }
                .show()
        }
    }

    private fun setReligion() {
        val religionButton = findViewById<Button>(R.id.profile_setting_religionButton)

        religionButton.setOnClickListener {
            val items = arrayOf("무교", "기독교", "불교", "천주교",
                "이슬람교", "힌두교", "개신교", "기타")
            var checkedItem = -1
            var i = 0

            for (item in items) {
                if (religion.equals(item)) {
                    checkedItem = i
                    break
                }
                i++
            }
            val builder = AlertDialog.Builder(this)
                .setTitle("자신의 종교를 하나 선택해주세요")
                .setSingleChoiceItems(items, checkedItem) { dialog, which ->
                    religion = items[which]
                }
                .setPositiveButton("선택") {dialog, which -> }
                .show()
        }
    }

    private fun setHobby() {
        val hobbyButton = findViewById<Button>(R.id.profile_setting_hobbyButton)

        hobbyButton.setOnClickListener {
            val items = arrayOf("영화보기", "독서하기", "맛집탐방", "운동하기",
                "캠핑하기", "코딩하기", "카페가기", "등산하기", "춤추기",
                "여행하기","쇼핑하기","산책하기","수다떨기","잠자기",
                "바둑하기", "수영하기", "악기연주", "그림그리기",
                "글쓰기", "노래하기", "요리하기", "게임하기")
            val checkedItems = booleanArrayOf(false, false, false, false, false,
                false, false, false, false, false, false,
                false, false, false, false, false, false,
                false, false, false, false, false)
            val selectedItemIndex = ArrayList<Int>()

            for (hobby in hobbies) {
                var i = 0
                for (item in items) {
                    if (hobby.equals(item)) {
                        checkedItems[i] = true
                        selectedItemIndex.add(i)
                        break
                    }
                    i++
                }
            }
            val builder = AlertDialog.Builder(this)
                .setTitle("자신의 취미를 여러개 선택해주세요")
                .setMultiChoiceItems(items, checkedItems){ dialogInterface: DialogInterface, i: Int, b: Boolean ->
                    if(b){
                        checkedItems[i] = true
                        selectedItemIndex.add(i)
                    } else if(selectedItemIndex.contains(i)) {
                        checkedItems[i] = false
                        selectedItemIndex.remove(i)
                    }
                }.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                    hobbies.clear()
                    for(j in selectedItemIndex) {
                        hobbies.add(items[j])
                    }
                }
                .show()
        }
    }

    private fun setAllEnable() {
        val photoButton = findViewById<ImageButton>(R.id.profile_setting_imageAddButton)

        photoButton.isEnabled = true
        for (editText in editTextArray) {
            editText.isEnabled = true
        }
        for (radioButton in radioButtonArray) {
            radioButton.isEnabled = true
        }
        for (button in buttonArray) {
            button.isEnabled = true
        }
    }

    private fun setAllDisable() {
        val photoButton = findViewById<ImageButton>(R.id.profile_setting_imageAddButton)

        photoButton.isEnabled = false
        for (editText in editTextArray) {
            editText.isEnabled = false
        }
        for (radioButton in radioButtonArray) {
            radioButton.isEnabled = false
        }
        for (button in buttonArray) {
            button.isEnabled = false
        }
    }

    private fun characterization() {
        val transformText = findViewById<TextView>(R.id.profile_setting_transform_text)

        transformText.text = "사진 변환중..."
        Thread {
            val animation = mutableMapOf<String, Any>()

            userDB = Firebase.database.reference.child("animation").child(auth.uid.toString())
            animation["request"] = "false" // server 가 request 를 처리했는지 판단
            animation["person"] = "false" // 사람인지 아닌지 판단
            userDB.updateChildren(animation)

            // connection request to Server
            try {
                socket = Socket(ip, port)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            // Write Buffer
            try {
                dos = DataOutputStream(socket.getOutputStream())
                dos.writeUTF(auth.uid.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
            requestFlag = false
            personFlag = false
            while (!requestFlag) {
                Thread.sleep(100)
            }
            // Thread.sleep(500)
            Handler(Looper.getMainLooper()).post {
                progressBar.visibility = View.INVISIBLE
                transformText.text = ""
                setAllEnable()
                if (personFlag) {
                    Toast.makeText(this@ProfileSettingActivity,"애니메이션 변환 완료!",Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileSettingActivity,"인물사진을 넣어주세요!",Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setPhoto() {
        val photoButton = findViewById<ImageButton>(R.id.profile_setting_imageAddButton)
        val imagesRef = storage.reference
            .child("photo/" + auth.uid.toString() + "/real.jpg")

        val getFromAlbumResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            progressBar.visibility = View.VISIBLE
            setAllDisable()
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data // 선택한 이미지의 주소
                // 이미지 파일 읽어와서 설정하기
                if (uri != null) {
                    // 사진 가져오기
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                    // 사진의 회전 정보 가져오기
                    val orientation = getOrientationOfImage(uri).toFloat()
                    // 이미지 회전하기
                    val newBitmap = getRotatedBitmap(bitmap, orientation)
                    imagesRef.putFile(uri).addOnSuccessListener {
                        characterization()
                    }
                }
            } else {
                progressBar.visibility = View.INVISIBLE
                setAllEnable()
            }
        }

        photoButton.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            getFromAlbumResultLauncher.launch(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getOrientationOfImage(uri: Uri): Int {
        // uri -> inputStream
        val inputStream = contentResolver.openInputStream(uri)
        val exif: ExifInterface? = try {
            ExifInterface(inputStream!!)
        } catch (e: IOException) {
            e.printStackTrace()
            return -1
        }
        inputStream.close()

        // 회전된 각도 알아내기
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        if (orientation != -1) {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> return 90
                ExifInterface.ORIENTATION_ROTATE_180 -> return 180
                ExifInterface.ORIENTATION_ROTATE_270 -> return 270
            }
        }
        return 0
    }

    @Throws(Exception::class)
    private fun getRotatedBitmap(bitmap: Bitmap?, degrees: Float): Bitmap? {
        if (bitmap == null) return null
        if (degrees == 0F) return bitmap
        val m = Matrix()
        m.setRotate(degrees, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    private fun checkInformation() : Boolean {
        val smokeGroup = findViewById<RadioGroup>(R.id.profile_setting_smoke_radioGroup)
        val drinkingGroup = findViewById<RadioGroup>(R.id.profile_setting_drink_radioGroup)

        nickname = findViewById<EditText>(R.id.profile_setting_inputNickname).text.toString()
        job = findViewById<EditText>(R.id.profile_setting_inputJob).text.toString()
        introMe = findViewById<EditText>(R.id.profile_setting_inputIntroduce).text.toString()

        smoke = when (smokeGroup.checkedRadioButtonId) {
            R.id.profile_setting_smoke_radioButton1 -> "비흡연"
            R.id.profile_setting_smoke_radioButton2 -> "흡연"
            R.id.profile_setting_smoke_radioButton3 -> "가끔"
            else -> ""
        }
        drinking = when (drinkingGroup.checkedRadioButtonId) {
            R.id.profile_setting_drink_radioButton1 -> "안함"
            R.id.profile_setting_drink_radioButton2 -> "자주"
            R.id.profile_setting_drink_radioButton3 -> "가끔"
            else -> ""
        }

        if (nickname.isEmpty() || job.isEmpty() || introMe.isEmpty() || smoke.isEmpty()
            || drinking.isEmpty() || mbti.isEmpty() || religion.isEmpty()
            || personalities.size == 0 || hobbies.size == 0 || !personFlag || !requestFlag)
            return false
        return true
    }

    private fun insertProfileInformation() {
        val profile = mutableMapOf<String, Any>()
        val animation = mutableMapOf<String, Any>()
        var personality : String =  ""
        var hobby : String = ""

        for (data in personalities) {
            personality += data
            if (!data.equals(personalities.last())) {
                personality += "/"
            }
        }
        for (data in hobbies) {
            hobby += data
            if (!data.equals(hobbies.last())) {
                hobby += "/"
            }
        }

        userDB = Firebase.database.reference.child("profile").child(auth?.uid.toString())
        if (birth.length > 0) {
            val age = SimpleDateFormat("yyyy-MM-dd-hh-mm")
                .format(System.currentTimeMillis())
                .split("-")[0].toInt() - birth.split("/")[0].toInt() + 1
            profile["birth"] = birth
            profile["gender"] = gender
            profile["age"] = age.toString()
        }
        profile["nickname"] = nickname
        profile["job"] = job
        profile["introMe"] = introMe
        profile["smoke"] = smoke
        profile["drinking"] = drinking
        profile["mbti"] = mbti
        profile["religion"] = religion
        profile["personality"] = personality
        profile["hobby"] = hobby
        profile["join"] = "true"
        userDB.updateChildren(profile)

        userDB = Firebase.database.reference.child("animation").child(auth.uid.toString())
        animation["permission"] = "true"
        userDB.updateChildren(animation)
    }

    private fun requestListener() {
        userDB = Firebase.database.getReference("animation").child(auth.uid.toString())
        userDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    if (ds.key.toString().equals("person") && ds.value.toString().equals("true")) {
                        personFlag = true
                    } else if (ds.key.toString().equals("request") && ds.value.toString().equals("true")) {
                        requestFlag = true
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showAnimationPhoto() {
        val dialog = AlertDialog.Builder(this).create()
        val edialog : LayoutInflater = LayoutInflater.from(this)
        val mView : View = edialog.inflate(R.layout.dialog_check_animation,null)
        val cancel : Button = mView.findViewById(R.id.dialog_check_animation_cancel)
        val save : Button = mView.findViewById(R.id.dialog_check_animation_save)

        //  취소 버튼 클릭 시
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        //  완료 버튼 클릭 시
        save.setOnClickListener {
            if (birth.length > 0) { // 회원가입 완료
                val intent = Intent(this@ProfileSettingActivity, BackgroundActivity::class.java)
                insertProfileInformation()
                startActivity(intent)
                finish()
            } else { // 프로필 재세팅 완료
                insertProfileInformation()
                finish()
            }
            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }

    private fun saveProfile() {
        val saveButton = findViewById<Button>(R.id.profile_setting_saveButton)

        saveButton.setOnClickListener {
            if (checkInformation()) {
                showAnimationPhoto()
            }
            else {
                if (!personFlag) {
                    Toast.makeText(this, "인물 사진을 넣어주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "정보를 완벽하게 입력해주세요!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


