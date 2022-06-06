package ru.irinavb.whatsupclone.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ru.irinavb.whatsupclone.activities.adapters.ChatsAdapter
import ru.irinavb.whatsupclone.activities.listeners.ChatClickListener
import ru.irinavb.whatsupclone.activities.listeners.FailureCallback
import ru.irinavb.whatsupclone.databinding.FragmentChatBinding
import ru.irinavb.whatsupclone.util.Chat
import ru.irinavb.whatsupclone.util.DATA_CHATS
import ru.irinavb.whatsupclone.util.DATA_USERS
import ru.irinavb.whatsupclone.util.DATA_USER_CHATS

class ChatFragment : Fragment(), ChatClickListener {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private var chatAdapter = ChatsAdapter(arrayListOf())

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val firebaseDb = FirebaseFirestore.getInstance()

    private var failureCallback: FailureCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (userId.isNullOrEmpty()) {
            failureCallback?.onUserError()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatAdapter.setOnClickListener(this)
        binding.chatsRecyclerView.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = chatAdapter

            firebaseDb.collection(DATA_USERS).document(userId!!).addSnapshotListener { document,
                                                                                       exception ->
                if (exception == null) {
                    refreshChats()
                }
            }
        }

        var chatList = ArrayList<String>()
        chatAdapter.updateChat(chatList)
    }

    private fun refreshChats() {
        firebaseDb.collection(DATA_USERS)
            .document(userId!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.contains(DATA_USER_CHATS)) {
                    val partners = documentSnapshot[DATA_USER_CHATS]
                    val chats = arrayListOf<String>()
                    for (partner in (partners as HashMap<String, String>).keys) {
                        if (partners[partner] != null) {
                            chats.add(partners[partner]!!)
                        }
                    }
                    chatAdapter.updateChat(chats)
                }
            }

    }

    fun newChat(partnerId: String) {
        firebaseDb
            .collection(DATA_USERS)
            .document(userId!!)
            .get()
            .addOnSuccessListener { userDocument ->
                val userChatPartners = hashMapOf<String, String>()
                if (userDocument[DATA_USER_CHATS] != null &&
                    userDocument[DATA_USER_CHATS] is HashMap<*, *>
                ) {
                    val userDocumentMap = userDocument[DATA_USER_CHATS] as HashMap<String, String>
                    if (userDocumentMap.containsKey(partnerId)) {
                        return@addOnSuccessListener
                    } else {
                        userChatPartners.putAll(userDocumentMap)
                    }
                }

                firebaseDb.collection(DATA_USERS)
                    .document(partnerId)
                    .get()
                    .addOnSuccessListener { partnerDocument ->
                        val partnerChatPartners = hashMapOf<String, String>()
                        if (partnerDocument[DATA_USER_CHATS] != null &&
                            partnerDocument[DATA_USER_CHATS] is HashMap<*, *>) {
                            val partnerDocumentMap = partnerDocument[DATA_USER_CHATS] as HashMap<String, String>
                            partnerChatPartners.putAll(partnerDocumentMap)
                        }
                        val chatParticipants = arrayListOf(userId, partnerId)
                        val chat = Chat(chatParticipants)
                        val chatRef = firebaseDb.collection(DATA_CHATS).document()
                        val userRef = firebaseDb.collection(DATA_USERS).document(userId)
                        val partnerRef = firebaseDb.collection(DATA_USERS).document(partnerId)

                        userChatPartners[partnerId] = chatRef.id
                        partnerChatPartners[userId] = chatRef.id

                        val batch = firebaseDb.batch()
                        batch.set(chatRef, chat)
                        batch.update(userRef, DATA_USER_CHATS, userChatPartners)
                        batch.update(partnerRef, DATA_USER_CHATS, partnerChatPartners)
                        batch.commit()
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setFailureCallbackListener(listener: FailureCallback) {
        failureCallback = listener
    }


    override fun onChatClicked(
        name: String?,
        otherUserId: String?,
        chatImageUrl: String?,
        chatName: String?
    ) {
        Toast.makeText(context, "$name clicked", Toast.LENGTH_SHORT).show()
    }
}