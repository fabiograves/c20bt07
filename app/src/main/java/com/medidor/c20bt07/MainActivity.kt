package com.medidor.c20bt07

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView


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
    private var bluetoothService: BluetoothService? = null

    companion object {
        private const val REQUEST_BLUETOOTH_SCAN_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerBt = findViewById(R.id.spinnerBt)
        textViewPesoBt = findViewById(R.id.textViewPesoBt)

        val buttonEntrarPesquisa = findViewById<Button>(R.id.buttonEntrarPesquisa)
            //Pagina Pesquisar
        }


    }
}