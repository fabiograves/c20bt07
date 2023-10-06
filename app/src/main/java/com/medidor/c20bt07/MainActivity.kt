package com.medidor.c20bt07

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues
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
import java.io.IOException
import java.util.UUID
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Criando BD
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "seu_banco_de_dados.db"
        private const val DATABASE_VERSION = 1

        // Nome da tabela e colunas
        const val TABLE_NAME = "registros"
        const val COLUMN_ID = "_id"
        const val COLUMN_BRINCO = "brinco"
        const val COLUMN_PESO = "peso"
        const val COLUMN_DATA = "data"
        const val COLUMN_HORA = "hora"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Crie a tabela
        val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_BRINCO TEXT," +
                "$COLUMN_PESO REAL," +
                "$COLUMN_DATA TEXT," +
                "$COLUMN_HORA TEXT)"
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Atualiza o banco de dados, se necessário
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}

class MainActivity : AppCompatActivity() {
    private val discoveredDevices: MutableList<BluetoothDevice> = ArrayList()
    private lateinit var textViewLog: TextView
    private lateinit var textViewPesoBt: TextView
    private lateinit var editTextBrinco: EditText
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

        // Inicialize as vistas
        buttonSalvarBrinco = findViewById(R.id.buttonSalvarBrinco)
        editTextBrinco = findViewById(R.id.editTextBrinco)
        textViewPesoBt = findViewById(R.id.textViewPesoBt)



        // Configurar um ouvinte de clique para o botão
        buttonSalvarBrinco.setOnClickListener {
            // Obter a data e hora atuais
            val dataHoraAtual = LocalDateTime.now()

            // Formatar a data atual no formato dd/mm/aaaa
            val dataAtual = dataHoraAtual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            // Formatar a hora atual no formato hh:mm (24 horas)
            val horaAtual = dataHoraAtual.format(DateTimeFormatter.ofPattern("HH:mm"))

            // Obter os valores dos campos
            val brinco = editTextBrinco.text.toString()
            val peso = textViewPesoBt.text.toString()
            val data = dataAtual
            val hora = horaAtual

            // Inserir esses valores no banco de dados
            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_BRINCO, brinco)
                put(DatabaseHelper.COLUMN_PESO, peso.toDouble()) // Converter para Double
                put(DatabaseHelper.COLUMN_DATA, data)
                put(DatabaseHelper.COLUMN_HORA, hora)
            }

            val newRowId = db?.insert(DatabaseHelper.TABLE_NAME, null, values)

            // Verifique se a inserção foi bem-sucedida
            if (newRowId != -1L) {
                // Inserção bem-sucedida, faça algo aqui se necessário
                Toast.makeText(this, "Registro inserido com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                // Ocorreu um erro durante a inserção
                Toast.makeText(this, "Erro ao inserir registro.", Toast.LENGTH_SHORT).show()
            }
        }

        // Verifique as permissões e inicie o Bluetooth
        checkAndInitializeBluetooth()

        // Defina os listeners dos botões

        // Inicie a descoberta de dispositivos Bluetooth
        startBluetoothDiscovery()

        textViewPesoBt = findViewById(R.id.textViewPesoBt)
        // Limita o tamanho do texto do textViewPesoBt a uma linha
        textViewPesoBt.maxLines = 1

        val buttonEntrarPesquisa = findViewById<Button>(R.id.buttonEntrarPesquisa)
        //Pagina Pesquisar

        // Inicializa o Bluetooth Manager e Adapter
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Obtém a lista de dispositivos pareados
        val devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices()

        // Cria um adaptador para o spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            devices.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Obtém o textViewLog
        val textViewLog = findViewById<TextView>(R.id.textViewLog)

        // Atualiza o texto do textViewLog
        fun atualizarLog(status: String) {
            runOnUiThread {
                // Adiciona um delay de 1 segundo
                Thread.sleep(1000)

                // Obtém o item selecionado no spinner
                val itemSelecionado = findViewById<Spinner>(R.id.spinnerBt).selectedItem as String

                // Verifica se o bluetoothSocket existe e está conectado
                if (bluetoothSocket == null || !bluetoothSocket!!.isConnected) {
                    // O Bluetooth não está conectado, exiba o texto "Desconectado"
                    textViewLog.text = "Desconectado"
                } else {
                    // O Bluetooth está conectado, exiba o texto "Conectado ao ${itemSelecionado}"
                    textViewLog.text = "Conectado ao ${itemSelecionado}"
                }
            }
        }

        // Vincula o spinner ao adaptador
        findViewById<Spinner>(R.id.spinnerBt).adapter = adapter

        // Obtém o botão buttonConectarBt
        val buttonConectarBt = findViewById<Button>(R.id.buttonConectarBt)

        // Função para desconectar o Bluetooth
        fun desconectarBluetooth() {
            // Desconecta o BluetoothSocket
            bluetoothSocket?.close()

            // Altera o texto do botão buttonConectarBt para Conectar
            buttonConectarBt.text = "Conectar"

            // Atualiza o texto do textViewLog
            atualizarLog("Desconectado")
        }

        // Vincula o onclick ao botão
        buttonConectarBt.setOnClickListener {
            // Obtém o item selecionado no spinner
            val itemSelecionado = findViewById<Spinner>(R.id.spinnerBt).selectedItem as String

            // Obtém o objeto BluetoothDevice correspondente ao item selecionado no spinner
            val dispositivoSelecionado = devices.find { it.name == itemSelecionado }

            // Verifica se o bluetoothSocket existe e está conectado antes de tentar conectar novamente
            if (bluetoothSocket == null || !bluetoothSocket!!.isConnected) {
                // O Bluetooth não está conectado, conecta ao dispositivo selecionado
                // Use um operador safe call para verificar se dispositivoSelecionado é nulo
                bluetoothSocket = dispositivoSelecionado?.createRfcommSocketToServiceRecord(
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                )

                try {
                    bluetoothSocket!!.connect()

                    // Inicia uma thread para receber os dados do Bluetooth
                    Thread {
                        val inputStream = bluetoothSocket!!.inputStream
                        val buffer = ByteArray(1024)
                        val valoresRecebidos = mutableListOf<String>()

                        while (true) {
                            try {
                                val bytes = inputStream.read(buffer)
                                val data = String(buffer, 0, bytes).trim()
                                val dataWithoutDot = data.trimEnd('.')

                                valoresRecebidos.add(dataWithoutDot)
                                if (valoresRecebidos.size > 30) {
                                    valoresRecebidos.removeAt(0)
                                }

                                val valorMaisFrequente =
                                    valoresRecebidos.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

                                // Atualiza o texto do textViewPesoBt com um delay de 1 segundo
                                runOnUiThread {
                                    textViewPesoBt.text = valorMaisFrequente ?: ""
                                }

                                // Adiciona um delay de 1 segundo
                                //Thread.sleep(1000)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }.start()

                    // Altera o texto do botão buttonConectarBt para Desconectar
                    buttonConectarBt.text = "Desconectar"
                    // O Bluetooth está conectado, exiba o texto "Conectado ao ${itemSelecionado}"
                    textViewLog.text = "Conectado ao ${itemSelecionado!!}"
                } catch (e: IOException) {
                    e.printStackTrace()
                    // Lida com a exceção de conexão Bluetooth aqui
                    textViewLog.text = "Não foi possível se conectar ao ${itemSelecionado!!}"
                }
            } else {
                // O Bluetooth já está conectado, desconecta o Bluetooth
                desconectarBluetooth()
            }
        }



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