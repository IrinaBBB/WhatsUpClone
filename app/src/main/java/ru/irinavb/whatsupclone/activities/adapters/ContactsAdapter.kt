package ru.irinavb.whatsupclone.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.irinavb.whatsupclone.R
import ru.irinavb.whatsupclone.activities.listeners.ContactsClickListener
import ru.irinavb.whatsupclone.util.Contact

class ContactsAdapter(private val contacts: ArrayList<Contact>) :
    RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    private var clickListener: ContactsClickListener? = null

    fun setOnItemClickListener(listener: ContactsClickListener) {
        clickListener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder =
        ContactsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contact, parent, false)
        )

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bind(contacts[position], listener = clickListener)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    class ContactsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val layout = view.findViewById<LinearLayout>(R.id.item_contact_layout)
        private val nameTextView = view.findViewById<TextView>(R.id.contact_name_text_view)
        private val phoneTextView = view.findViewById<TextView>(R.id.contact_phone_text_view)

        fun bind(contact: Contact, listener: ContactsClickListener?) {
            nameTextView.text = contact.name
            phoneTextView.text = contact.phone
            layout.setOnClickListener { listener?.onContactClicked(contact.name, contact.phone) }
        }
    }
}