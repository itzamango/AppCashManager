package com.example.cashmanager.ui.send

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cashmanager.*
import com.example.cashmanager.SpinnerAdapter
import com.example.cashmanager.data.models.SendTran
import com.example.cashmanager.databinding.FragmentSendBinding
import com.example.cashmanager.ui.CurrencyTransferViewModel
import com.example.cashmanager.ui.user.PREF_NAME
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.NumberFormatException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SendFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SendFragment : Fragment() {
    private lateinit var viewModel: CurrencyTransferViewModel
    private lateinit var binding: FragmentSendBinding
    //private lateinit var binding: FragmentSendBinding

    val items= listOf("MXN", "DLS", "YEN")
    val itemsprice= listOf(1, 1, 1)

    // To get the data
    //private var viewModel: SendViewModel?= null
    private var _asset: String = ""
    private var _amount: Double = 0.0
    private var _address: String = ""
    private var _total: Double = 0.0

    // Notifications Channel ID
    val CHANNEL_SEND = "TRANSACTIONS"


    private lateinit var header: View

    private lateinit var usernameAppbar: TextView
    private lateinit var emailAppbar: TextView
    private lateinit var preferences: SharedPreferences


    var amount_currency = true

    //This boolean is used to track wether as criptocurrency was set before clicking
    //compare endicon in amount text field
    var error = false

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        preferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_send,
            container,
            false
        )

        viewModel = CurrencyTransferViewModel(
            (requireContext().applicationContext as CurrencyTransfersApplication).currencyTransactionRepository
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.appBar)
        ActionBarDrawerToggle(view?.context as Activity?,binding.drawerLayout, binding.appBar, R.string.open_drawer, R.string.close_drawer)

        header = binding.navView.getHeaderView(0)
        usernameAppbar = header.findViewById(R.id.userNameAppbar)
        emailAppbar = header.findViewById(R.id.emailAppbar)

        val mAuth = FirebaseAuth.getInstance()

        emailAppbar.text = mAuth.currentUser?.email.toString()

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.logout -> {
                    mAuth!!.signOut()

                    findNavController().navigate(R.id.action_sendFragment_to_loginFragment, null)
                }
            }
            true
        }

        val adapter = SpinnerAdapter(requireContext(), items,getAssets_Short())
        (binding.assetSend.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        // NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setNotificationChannel()
        }

        binding.qrScanBtn.setOnClickListener{
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle("Importar desde QR")
            builder.setMessage("Escanear QR")
            // Read QR from camera
            builder.setPositiveButton("Camara", { dialogInterface: DialogInterface, i: Int ->
                setUpQRCode()
            })
            //Read QR from gallery
            builder.setNegativeButton("Galeria", { dialogInterface: DialogInterface, i: Int ->
                readImage()
            })
            builder.show()
        }

        binding.amountValue.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode >=7 && keyCode <= 16 && event.action == KeyEvent.ACTION_UP) {
                val textamount = binding.amountValue.text

                if (textamount != null) {
                    if(textamount.isNotEmpty()) {
                        val amount = textamount.toString().toDouble()
                        val fee: Double = 0.01

                        if(amount_currency) {
                            val _total = amount.plus(fee)
                            binding.textView11.text = _total.toString()
                        } else{
                            var currency = binding.assetText.text.toString()
                            var amount_converted : Double = 0.0


                            when(currency){
                                "MXN" ->  amount_converted =  amount / itemsprice[0]

                                "DLS" ->   amount_converted =  amount / itemsprice[1]

                                "YEN" ->   amount_converted =  amount / itemsprice[2]
                            }

                            _total = amount_converted.plus(fee)
                            binding.textView11.text = _total.toString()
                        }


                        return@OnKeyListener true
                    }
                }

                false
            }
            false
        })

        binding.addressEditText.setOnFocusChangeListener { v, hasFocus ->

            if(hasFocus) {
                binding.addressInputLayout.helperText = "Numero de Destino"

            } else {

                val textamount = binding.addressEditText.text

                if (textamount != null) {
                    if (textamount.isNotEmpty() && textamount.length > 2) {

                        val address = textamount.toString()
                        val currency = binding.assetText.text.toString()

                        if (currency == "MXN") {

                            when {
                                address.startsWith("0") -> binding.addressInputLayout.helperText =
                                    "Puede omitir el cero inicio"
                                //else -> binding.addressInputLayout.error = ""
                            }

                        } else {
                            if (address.startsWith("0")) binding.addressInputLayout.helperText =
                                "Puede omitir el cero al inicio"
                            //else binding.addressInputLayout.error = ""

                        }
                    } else binding.addressInputLayout.error = "Destino muy corto. Reviselo."
                }
            }
        }


        binding.amountTextField.setEndIconOnClickListener{
            try {
                var currency = binding.assetText.text.toString()
                var amount = binding.amountValue.text.toString().toDouble()
                var amount_converted = 0.0


                when (amount_currency) {

                    true -> when (currency) {
                        "MXN" -> {
                            amount_converted = itemsprice[0] * amount
                            error = false
                        }
                        "DLS" -> {
                            amount_converted = itemsprice[1] * amount
                            error = false
                        }
                        "YEN" -> {
                            amount_converted = itemsprice[2] * amount
                            error = false
                        }
                        else -> {
                            binding.amountTextField.error = getString(R.string.cm_amount_error)
                            error = true
                        }
                    }

                    false -> when (currency) {
                        "MXN" -> {
                            amount_converted = amount / itemsprice[0]
                            error = false
                        }
                        "DLS" -> {
                            amount_converted = amount / itemsprice[1]
                            error = false
                        }
                        "YEN" -> {
                            amount_converted = amount / itemsprice[2]
                            error = false
                        }
                        else -> {
                            binding.amountTextField.error = getString(R.string.cm_amount_error)
                            error = true
                        }
                    }

                }

                if (!error) {
                    binding.amountTextField.error = null

                    if (amount_currency) {
                        binding.amountTextField.prefixText = "$"
                        binding.amountTextField.suffixText = null
                    } else {
                        binding.amountTextField.prefixText = null
                        binding.amountTextField.suffixText = currency
                    }

                    amount_currency = !amount_currency

                    if (!amount_currency) {
                        var text = binding.amountValue.text.toString() + currency
                        binding.amountTextField.helperText = text
                    } else {
                        var text = "$" + binding.amountValue.text.toString()
                        binding.amountTextField.helperText = text
                    }

                    binding.amountValue.setText(amount_converted.toString())
                }
            }catch (e:Exception){
                Toast.makeText(requireContext(), "Seleccione una monera y " +
                        "cantidad", Toast.LENGTH_LONG).show()
            }
        }


        binding.button2.setOnClickListener {
            findNavController().navigate(R.id.action_sendFragment_to_dashboardFragment, null)
        }


        binding.button5.setOnClickListener {
            try {
                _asset = binding.assetText.text.toString()
                _amount = binding.amountValue.text.toString().toDouble()
                _address = binding.addressEditText.text.toString()
                if (validateInformation(_asset, _amount, _address)) {
                    viewModel.newCurrencyTransferSend()
                    openConfirmationDialog(_asset, _amount, _address)
                    CoroutineScope(Dispatchers.IO).launch{
                        delay(3_000)
                        (activity as MainActivity).simpleNotification("Envio Satisfactorio", "Notificacion de envio satifactorio", 21)
                    }
                } else {
                    Toast.makeText(requireContext(), "Ingrese la informacion \n" +
                            "Moneda, Cantidad o Destino falta", Toast.LENGTH_LONG).show()
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Ingrese la informacion\n" +
                        "Moneda, Cantidad o Destino falta", Toast.LENGTH_LONG).show()
            }
        }

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
         * @return A new instance of fragment SendFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SendFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun validateInformation(asset: String, amount: Double, address: String): Boolean {
        return asset.isNotEmpty() && amount.toString().isNotEmpty() && address.isNotEmpty()
    }

    private fun getAssets_Short(): MutableList<Asset_Short>{
        var assets:MutableList<Asset_Short> = ArrayList()

        assets.add(Asset_Short("MXN",R.drawable.ic_mxn))
        assets.add(Asset_Short("DLS", R.drawable.ic_dls))
        assets.add(Asset_Short("YEN",R.drawable.ic_yen))

        return assets
    }

    fun openConfirmationDialog(asset:String, amount: Double, address: String){
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Detalles de envio:")
        builder.setMessage("Enviar $amount $asset a $address ?")
        builder.setPositiveButton("Enviar", { dialogInterface: DialogInterface, i: Int ->
            loadingTransactionMessage()

            findNavController().navigate(R.id.action_sendFragment_to_dashboardFragment, null)
        })
        builder.setNegativeButton("Cancelar", { dialogInterface: DialogInterface, i: Int ->
            Toast.makeText(requireContext(), "Envio cancelado", Toast.LENGTH_LONG).show()
        })
        builder.show()
    }

    private fun setNotificationChannel() {
        val name = getString(R.string.channel_courses)
        val descriptionText = getString(R.string.courses_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_SEND, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    private fun progressNotification() {
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_SEND)
            .setContentTitle(getString(R.string.transactions_title))
            .setContentText(getString(R.string.transaction_description))
            .setSmallIcon(R.drawable.ic_monetization)

        val max = 20
        var progress = 0
        var percentage = 0
        val handler = Handler()

        with(NotificationManagerCompat.from(requireContext())) {
            builder.setProgress(max, progress, true)
            notify(34, builder.build())

            Thread(Runnable {
                while (progress < max) {
                    progress += 1

                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {

                    }
                    handler.post(Runnable {
                        if (progress == max){
                            builder.setContentText("Envio Satifactorio")
                            builder.setProgress(0,0,false)
                        }else{
                            // Revisar porque no me sale porcentaje
                            percentage = (progress*100)/max
                            builder.setContentText("$percentage% complete")
                            builder.setProgress(max,progress,true)
                        }
                        notify(34, builder.build())
                    })
                }

            }).start()


        }
    }

    fun saveTransfer() {
        val transfer = SendTran(
            assetType = _asset,
            totalAmount = _amount,
            emailAddress = _address,
            feeValue = 0.01f,
            totalTransfer = 400f
        )

        //viewModel?.insertTransfer(transfer)
    }

    fun loadingTransactionMessage(){
        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.setMessage("Processing transaction...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        Handler().postDelayed({progressDialog.dismiss()},3000)
    }

    // Scanner settings
    private fun setUpQRCode(){
        //Toast.makeText(requireContext(), "Option not available", Toast.LENGTH_SHORT).show()
        IntentIntegrator(requireActivity())
            .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE) // Selecting the type of code to scan
            .setTorchEnabled(false) // Flash enabled / disabled
            .setBeepEnabled(true)   // Sound activated when scanning
            .setPrompt("Scan the QR Code please")  // Message that appears when scanning
            .initiateScan()
    }

    private fun readImage(){
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")

        startActivityForResult(pickIntent, 111)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            111 -> {
                if (data == null || data.data == null) {
                    Toast.makeText(requireContext(), "Data import was canceled", Toast.LENGTH_SHORT).show()
                    return
                }
                val uri: Uri = data.data!!
                try {
                    val inputStream: InputStream? = activity?.contentResolver?.openInputStream(uri)
                    var bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap == null) {
                        Toast.makeText(requireContext(), "Image format is not supported", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val width = bitmap.width
                    val height = bitmap.height
                    val pixels = IntArray(width * height)
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                    bitmap.recycle()
                    val source = RGBLuminanceSource(width, height, pixels)
                    val bBitmap = BinaryBitmap(HybridBinarizer(source))
                    val reader = MultiFormatReader()
                    try {
                        val result = reader.decode(bBitmap)
                        val output: List<String> = result.getText().split("\n")
                        // We write the scan results in the text fields
                        if (output.size==3) {
                            binding.amountValue.setText(output[0])
                            binding.assetText.setText(output[1])
                            binding.addressEditText.setText(output[2])
                            when (output[0]){
                                "MXN" -> {binding.addressEditText.setText("MXN"+output[2])}
                                "DLS" -> {binding.addressEditText.setText("DLS"+output[2])}
                                "YEN" -> {binding.addressEditText.setText("YEN"+output[2])}

                            }
                            Toast.makeText(requireContext(),"Importacion correcta", Toast.LENGTH_SHORT).show()
                        } else{
                            Toast.makeText(requireContext(), "QR not valid", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: NotFoundException) {
                        Log.e("TAG", "decode exception", e)
                        Toast.makeText(requireContext(), "Data import was not successful", Toast.LENGTH_SHORT).show()
                        Toast.makeText(requireContext(), "Please verify you are reading a QR code", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: FileNotFoundException) {
                    Log.e("TAG", "can not open file" + uri.toString(), e)
                }
            }
            else -> {
                val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

                if (result != null){
                    if (result.contents == null) {
                        Toast.makeText(requireContext(), "Escaner cancelado", Toast.LENGTH_LONG).show()
                    } else {
                        // We write the scan results in the text fields
                        val resultado = result.contents
                        val output: List<String> = resultado.split("\n")
                        if (output.size==3) {
                            binding.amountValue.setText(output[0])
                            binding.assetText.setText(output[1])
                            when (output[0]){
                                "MXN" -> {binding.addressEditText.setText("MXN"+output[2])}
                                "DLS" -> {binding.addressEditText.setText("DLS"+output[2])}
                                "YEN" -> {binding.addressEditText.setText("YEN"+output[2])}

                            }
                            Toast.makeText(requireContext(), "Escaneo satisfacctorio", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "QR no valido", Toast.LENGTH_LONG).show()
                        }
                    }
                }else{
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

}