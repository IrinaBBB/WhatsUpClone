package ru.irinavb.whatsupclone.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.irinavb.whatsupclone.activities.adapters.ContactsAdapter
import ru.irinavb.whatsupclone.activities.listeners.ContactsClickListener
import ru.irinavb.whatsupclone.databinding.ActivityContactsBinding
import ru.irinavb.whatsupclone.util.Contact

class ContactsActivity : AppCompatActivity(), ContactsClickListener {

    private lateinit var binding: ActivityContactsBinding
    private val contactsList = ArrayList<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getContacts()
    }

    @SuppressLint("Range")
    private fun getContacts() {
        binding.loading.visibility = View.VISIBLE
        contactsList.clear()
        val newList = ArrayList<Contact>()
        val phones = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )
        while (phones!!.moveToNext()) {
            val name =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(
                phones.getColumnIndex(
                    ContactsContract
                        .CommonDataKinds
                        .Phone.NUMBER
                )
            )
            newList.add(Contact(name, phoneNumber))
        }
        contactsList.addAll(newList)
        phones.close()

        setUpList()
    }

    private fun setUpList() {
        val contactAdapter = ContactsAdapter(contactsList)
        contactAdapter.setOnItemClickListener(this)
        val recyclerView = binding.contactsRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this@ContactsActivity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this, DividerItemDecoration
                    .VERTICAL
            )
        )
        recyclerView.adapter = contactAdapter
        binding.loading.visibility = View.GONE
    }

    override fun onContactClicked(name: String?, phone: String?) {
        val intent = Intent().apply {
            putExtra(MainActivity.PARAM_NAME, name)
            putExtra(MainActivity.PARAM_PHONE, phone)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, ContactsActivity::class.java)
    }
}