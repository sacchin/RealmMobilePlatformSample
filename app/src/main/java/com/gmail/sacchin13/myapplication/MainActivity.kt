package com.gmail.sacchin13.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import io.realm.*
import org.jetbrains.anko.textColor
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private var list: LinearLayout by Delegates.notNull()
    private var edit: EditText by Delegates.notNull()
    private var realm: Realm by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()

        val user = User.currentUser()
        if (user == null) {
            val intent = Intent(this, LoginActivity().javaClass)
            startActivityForResult(intent, 0)
        }else{
            initRealm()
        }
    }

    fun initView(){
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        list = findViewById(R.id.list) as LinearLayout
        edit = findViewById(R.id.edit) as EditText
        edit.setOnEditorActionListener(OnEditListener())

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view -> updateView() }
    }

    fun initRealm(){
        val user = User.currentUser()
        //同期させたくないユーザ固有のデータを扱う場合は以下のようにする
        //val serverURL = "realm://xx.xx.xx.xx:9080/${user.identity}/default"
        val serverURL = "realm://xx.xx.xx.xx:9080/~/default"
        val configuration = SyncConfiguration.Builder(user, serverURL).build()

        realm = Realm.getInstance(configuration)
        realm.addChangeListener(OnRealmChangeListener())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val user = User.currentUser()
        if (user != null) {
            Log.v("onActivityResult", "created! " + user.toJson())
        }
        initRealm()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            realm.close()
            User.currentUser()?.logout()
            val intent = Intent(this, LoginActivity().javaClass)
            startActivityForResult(intent, 0)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun createTextView(message: Message, mine: Boolean): TextView{
        val tv = TextView(this)
        tv.text = message.message
        if(mine) tv.textColor = Color.RED
        tv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        return tv
    }

    fun saveMessage(text: String){
        val user = User.currentUser()

        realm.executeTransaction {
            val temp = realm.createObject(Message::class.java)
            temp.time = System.currentTimeMillis()
            temp.message = text
            temp.user = user.identity
        }
    }

    inner class OnEditListener() : TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            if (actionId === EditorInfo.IME_ACTION_SEND) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v!!.windowToken, 0)
                saveMessage(v.text.toString())
                edit.setText("")
            }
            return false
        }
    }

    inner class OnRealmChangeListener() : RealmChangeListener<Realm?> {
        override fun onChange(element: Realm?) {
            updateView()
        }
    }

    fun updateView(){
        list.removeAllViews()
        val messages = realm.where(Message::class.java).findAllSorted("time", Sort.DESCENDING)
        val user = User.currentUser()
        for(m in messages) {
            list.addView(createTextView(m, user.identity == m.user))
        }
    }

}
