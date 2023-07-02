package com.example.cashmanager.ui.receive

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cashmanager.Asset_Short
import com.example.cashmanager.R
import com.example.cashmanager.SpinnerAdapter
import com.example.cashmanager.data.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.cashmanager.CurrencyTransfersApplication
import com.example.cashmanager.databinding.FragmentReceiveBinding
import com.example.cashmanager.ui.CurrencyTransferViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReceiveFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
const val CHANNEL_RECEIVE = "CHANNER_RECEIVE"
class ReceiveFragment : Fragment() {
    private lateinit var viewModel: CurrencyTransferViewModel
    private lateinit var binding: FragmentReceiveBinding

    val items= listOf("MXN", "DLS", "YEN")

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val width = 1000
    private val height = 1000
    private val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setNoticationChannelQR()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_receive,
            container,
            false
        )

        viewModel = CurrencyTransferViewModel(
            (requireContext().applicationContext as CurrencyTransfersApplication).currencyTransactionRepository
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val adapter = SpinnerAdapter(requireContext(), items,getAssets_Short())
        (binding.assetTransaction.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.appBar.setNavigationOnClickListener {

            findNavController().navigate(R.id.action_receiveFragment_to_dashboardFragment, null)}

        // Generate QR Code
        binding.qrGenerateBtn.setOnClickListener{
            if(binding.amountToSendValue.text.toString().isEmpty() || binding.assetTextTransaction.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter all required information.\n" +
                        "Amount / Asset field is empty", Toast.LENGTH_LONG).show()
            }else{
                createQR(binding.amountToSendValue.text.toString(), binding.assetTextTransaction.text.toString())
                binding.qrGenerateBtn.isVisible = false
                binding.cmTransactionCopyBtn.isVisible = true
                binding.cmTransactionShareBtn.isVisible = true
                binding.cmTransactionDownloadBtn.isVisible = true
                binding.amountToSendValue.isEnabled = false
                binding.assetTextTransaction.isEnabled = false
//                saveObject()
                viewModel.newCurrencyTransfer()
                QrGeneratedNotification()
            }
        }

        // Download the QR Code
        binding.cmTransactionDownloadBtn.setOnClickListener{
            downloadQR()
        }

        binding.cmTransactionShareBtn.setOnClickListener{
            shareImage()
        }

        binding.cmTransactionCopyBtn.setOnClickListener{
            getClipboard(requireContext())
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.cm_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReceiveFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReceiveFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    //generamos datos dummy con este método
    private fun getAssets_Short(): MutableList<Asset_Short>{
        var assets:MutableList<Asset_Short> = ArrayList()

        assets.add(Asset_Short("MXN",R.drawable.ic_mxn))
        assets.add(Asset_Short("DLS", R.drawable.ic_dls))
        assets.add(Asset_Short("YEN",R.drawable.ic_yen))

        return assets
    }

    fun shareImage() {
        val drawable = binding.imageView2.drawable as BitmapDrawable
        val bitmap = drawable.bitmap as Bitmap
        val bitmapPath = MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, "title", null) as String

        val uri = Uri.parse(bitmapPath)

        val intent = Intent(Intent.ACTION_SEND)
        // intent
        intent.type = "image/png"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.putExtra(Intent.EXTRA_TEXT, "Transaction")
        startActivity(Intent.createChooser(intent, "Share to"))
    }

    // copy filename
    fun getClipboard(context: Context) {
        val drawable = binding.imageView2.drawable as BitmapDrawable
        val bitmap = drawable.bitmap as Bitmap
        val bitmapPath = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "title", null) as String
        val uri = Uri.parse(bitmapPath)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("simple text", "QR_wallet.jpg")
//        val clip: ClipData = ClipData.newUri(context.contentResolver, "image", uri)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Filename copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun createQR(amount: String, asset:String): Boolean {
        val text = amount + '\n' + asset + '\n' + "@cashmanager.com"
        if (text.isNotBlank()){
            val codeWriter = MultiFormatWriter()
            try{
                val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
                for(x in 0 until width){
                    for (y in 0 until height){
                        bitmap.setPixel(x, y, if(bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                binding.imageView2.setImageBitmap(bitmap)
                Toast.makeText(requireContext(), "QR code has been created successfully", Toast.LENGTH_LONG).show()
                return true
            } catch (e: WriterException) {
                Toast.makeText(requireContext(), "Error writing the QR code", Toast.LENGTH_LONG).show()
                return false
            }
        }
        return true
    }

    private fun downloadQR(){
        // Download the QR code in Gallery
        try {
            val filename = "QR_wallet.jpg"
            val file = File(requireContext().externalMediaDirs.first(), filename)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.flush()
            out.close()
            Toast.makeText(requireContext(), "Imagen guardada en galeria!: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al guardar en galeria", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    // Method to register notifications related to QR generator
    private fun setNoticationChannelQR() {
        val name = getString(R.string.qr_notifications)
        val descriptionText = getString(R.string.qr_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_RECEIVE, name, importance).apply {
            description = descriptionText
        }

        val notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        notificationManager.createNotificationChannel(channel)
    }

    private fun QrGeneratedNotification(){
        // Method to notify the user that has generated a new qr code
        var notification = NotificationCompat.Builder(requireContext(), CHANNEL_RECEIVE)
            .setSmallIcon(R.drawable.ic_logo)
            .setColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
            .setContentTitle(getString(R.string.simple_title))
            .setContentText(getString(R.string.qr_generator_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(requireContext()).run {
            // ids para saber que notificacion es
            notify(20, notification)
        }
    }

}
