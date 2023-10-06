package com.medidor.c20bt07

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import java.util.UUID


class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val discoveredDevices: MutableList<BluetoothDevice> = ArrayList()
    private lateinit var textViewLog: TextView
    private lateinit var textViewPesoBt: TextView
    private lateinit var edittextBrinco: EditText
    private lateinit var buttonSalvarBrinco: Button
    private lateinit var spinnerBt: Spinner
    private lateinit var buttonConectarBt: Button
    private lateinit var buttonEntrarPesquisa: Button

    private var bluetoothData: String = ""
    //private var bluetoothService: BluetoothService? = null

    companion object {
        private const val REQUEST_BLUETOOTH_SCAN_PERMISSION = 1
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerBt = findViewById(R.id.spinnerBt)
        textViewPesoBt = findViewById(R.id.textViewPesoBt)

        val buttonEntrarPesquisa = findViewById<Button>(R.id.buttonEntrarPesquisa)
        //Pagina Pesquisar

        val adaptador = ArrayAdapter<Any>(
            this,
            android.R.layout.simple_spinner_item,
            arrayListOf()
        )

        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerBt.adapter = adaptador

        // Atualiza a lista de dispositivos conectados

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        bluetoothAdapter.startDiscovery()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action

                if (action == BluetoothDevice.ACTION_FOUND) {
                    // Adiciona o dispositivo à lista
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    adaptador.add(device.name)
                }
            }
        }

        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        val buttonConectarBt = findViewById<Button>(R.id.buttonConectarBt)

        buttonConectarBt.setOnClickListener {
            val device = spinnerBt.selectedItem as? BluetoothDevice

            if (device == null) {
                // Nenhum dispositivo foi selecionado
                return
            }

            val MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"
            val bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(MY_UUID))

            bluetoothSocket.connect()

            // Inicia o fluxo de dados

            val inputStream = bluetoothSocket.inputStream
                .bufferedReader()
                .readLines()

            // Exibe o peso

            textViewPesoBt.text = inputStream[0]
        }
    }
}