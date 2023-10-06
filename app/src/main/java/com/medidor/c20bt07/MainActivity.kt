package com.medidor.c20bt07

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val discoveredDevices: MutableList<BluetoothDevice> = ArrayList()
    private lateinit var textViewLog: TextView
    private lateinit var textViewPesoBt: TextView
    private lateinit var edittextBrinco: EditText
    private lateinit var buttonSalvarBrinco: Button
    private lateinit var spinnerBt: Spinner
    private lateinit var buttonConectarBt: Button
    private lateinit var buttonEntrarPesquisa: Button
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1

    private var bluetoothData: String = ""
    //private var bluetoothService: BluetoothService? = null

    companion object {
        private const val REQUEST_BLUETOOTH_SCAN_PERMISSION = 1
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Verifique as permissões e inicie o Bluetooth
        checkAndInitializeBluetooth()

        // Defina os listeners dos botões

        // Inicie a descoberta de dispositivos Bluetooth
        startBluetoothDiscovery()

        spinnerBt = findViewById(R.id.spinnerBt)
        textViewPesoBt = findViewById(R.id.textViewPesoBt)

        val buttonEntrarPesquisa = findViewById<Button>(R.id.buttonEntrarPesquisa)
        //Pagina Pesquisar

        // Inicializa o Bluetooth Manager e Adapter
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter


    }

    private fun checkAndInitializeBluetooth() {
        // Verifique as permissões e inicialize o Bluetooth
        if (checkBluetoothPermissions()) {
            initializeBluetooth()
        } else {
            requestBluetoothPermissions()
        }
    }

    private fun checkBluetoothPermissions(): Boolean {
        // Verifique se as permissões BLUETOOTH_ADMIN e ACCESS_FINE_LOCATION foram concedidas
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 2)
            return false
        }
        return true
    }

    private fun requestBluetoothPermissions() {
        // Solicite as permissões BLUETOOTH_ADMIN e ACCESS_FINE_LOCATION e BLUETOOTH_CONNECTION ao usuário

    }

    private fun initializeBluetooth() {
        // Inicialize o Bluetooth aqui
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Verifique se o Bluetooth está disponível e habilite-o se não estiver

        // ... (outros inicializações relacionadas ao Bluetooth)
    }

    // Função para verificar se a conexão Bluetooth está ativa
    private fun isBluetoothConnected(): Boolean {
        return bluetoothSocket != null && bluetoothSocket!!.isConnected
    }

    private fun startBluetoothDiscovery() {
        // Inicie a descoberta de dispositivos Bluetooth aqui
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothAdapter.startDiscovery()

        // Registre o BroadcastReceiver para lidar com dispositivos encontrados
        registerBluetoothDiscoveryReceiver()
    }

    private fun registerBluetoothDiscoveryReceiver() {
        // Registre o BroadcastReceiver para lidar com dispositivos encontrados
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action

                if (action == BluetoothDevice.ACTION_FOUND) {
                    // Trate os dispositivos encontrados e atualize a lista
                    handleDiscoveredDevice(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))
                }
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    private fun handleDiscoveredDevice(device: BluetoothDevice?) {
        // Trate os dispositivos Bluetooth encontrados e atualize a lista no Spinner
    }

    // Adicione outras funções relacionadas à conexão Bluetooth e desconexão aqui

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeBluetooth()
            } else {
                // Trate o caso em que o usuário recusou permissões
            }
        }
    }
}