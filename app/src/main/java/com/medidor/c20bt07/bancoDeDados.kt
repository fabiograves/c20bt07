package com.medidor.c20bt07

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.FileProvider
import java.text.SimpleDateFormat
import java.util.Locale
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File

data class Brinco(val id: Long, val peso: Float, val nome: String, val dataHoraCadastro: Date)

class BrincosAdapter(var brincosList: MutableList<Brinco>, private val context: Context) : RecyclerView.Adapter<BrincosAdapter.ViewHolder>() {

    private val selectedIds = mutableSetOf<Long>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pesoTextView: TextView = itemView.findViewById(R.id.pesoTextView)
        val nomeTextView: TextView = itemView.findViewById(R.id.nomeTextView)
        val dataHoraCadastroTextView: TextView = itemView.findViewById(R.id.dataHoraCadastroTextView)
        val selectCheckBox: CheckBox = itemView.findViewById(R.id.selectCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_brinco, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val brinco = brincosList[position]
        val context = holder.itemView.context
        holder.pesoTextView.text = context.getString(R.string.label_peso, brinco.peso.toString())
        holder.nomeTextView.text = context.getString(R.string.label_nome, brinco.nome)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dataHoraFormatada = dateFormat.format(brinco.dataHoraCadastro)
        holder.dataHoraCadastroTextView.text = context.getString(R.string.label_data_hora, dataHoraFormatada)
        holder.selectCheckBox.isChecked = selectedIds.contains(brinco.id)

        holder.selectCheckBox.setOnClickListener {
            if (holder.selectCheckBox.isChecked) {
                selectedIds.add(brinco.id)
            } else {
                selectedIds.remove(brinco.id)
            }
        }
    }

    fun getSelectedItems(): List<Brinco> {
        return brincosList.filter { selectedIds.contains(it.id) }
    }

    fun removeSelectedItems() {
        brincosList.removeAll { selectedIds.contains(it.id) }
        selectedIds.clear()
    }

    override fun getItemCount(): Int {
        return brincosList.size
    }

    fun setSelected(itemId: Long, selected: Boolean) {
        if (selected) {
            selectedIds.add(itemId)
        } else {
            selectedIds.remove(itemId)
        }
    }

    fun gerarRelatorioPDF(itensSelecionados: List<Brinco>, context: Context) {
        // Crie um arquivo PDF na pasta de cache
        val pdfFile = File(context.cacheDir, "relatorio.pdf")

        // Crie um arquivo de documento PDF
        val pdfWriter = PdfWriter(pdfFile.path)
        val pdfDocument = PdfDocument(pdfWriter)
        val pdf = Document(pdfDocument)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        // Adicione a data de geração do relatório no topo
        val dataDeGeracao = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        pdf.add(Paragraph("Relatório de Itens Selecionados - Gerado em: $dataDeGeracao"))

        // Defina o espaçamento entre as linhas (ajuste conforme necessário)
        val spacingBetweenLines = -10f

        // Adicione os itens selecionados ao PDF
        for (item in itensSelecionados) {
            //pdf.add(Paragraph("ID: ${item.id}"))
            //pdf.add(Paragraph("").setMarginBottom(spacingBetweenLines))
            pdf.add(Paragraph("Número: ${item.nome}"))
            pdf.add(Paragraph("").setMarginBottom(spacingBetweenLines))
            pdf.add(Paragraph("Peso: ${item.peso}"))
            pdf.add(Paragraph("").setMarginBottom(spacingBetweenLines))

            // Formate a data no formato "dd/MM/yyyy HH:mm" e adicione-a ao PDF
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dataFormatada = dateFormat.format(item.dataHoraCadastro)
            pdf.add(Paragraph("Data do Cadastro: $dataFormatada"))
            pdf.add(Paragraph("").setMarginBottom(spacingBetweenLines))

            pdf.add(Paragraph("----------------------------------------------------"))

            // Defina o espaço entre as linhas para o próximo parágrafo
            //pdf.add(Paragraph("").setMarginBottom(spacingBetweenLines))
        }

        // Feche o documento PDF
        pdf.close()

        // Crie uma URI de arquivo segura usando FileProvider
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)

        // Abra o PDF usando um aplicativo de visualização de PDF
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Conceda permissão de leitura à URI
        context.startActivity(intent)
    }

    fun desmarcarTodos() {
        selectedIds.clear()
        notifyDataSetChanged()
    }

}

class bancoDeDados : AppCompatActivity() {

    private lateinit var database: AppDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextPesquisarNome: EditText
    private lateinit var editTextPesquisarData: EditText
    private lateinit var btPesquisarBd: Button
    private lateinit var btSelecionarTodos: Button
    private lateinit var btGerarPdf: Button
    private lateinit var btDesmarcarTudo: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_pesquisar)

        // Evento gerar PDF selecionados
        val btGerarPdf = findViewById<Button>(R.id.btGerarPdf)
        btGerarPdf.setOnClickListener {
            val adapter = recyclerView.adapter as BrincosAdapter
            val itensSelecionados = adapter.getSelectedItems()

            if (itensSelecionados.isNotEmpty()) {
                adapter.gerarRelatorioPDF(itensSelecionados, this) // Passe o contexto da Activity
            } else {
                // Exiba uma mensagem de erro ou aviso, pois nenhum item foi selecionado.
            }
        }

        // Evento bt excluir selecionados
        val btExluirSelecionado = findViewById<Button>(R.id.btExcluirSelecionado)
        btExluirSelecionado.setOnClickListener {
            excluirItensSelecionados()
        }

        // Selecionar todos
        btSelecionarTodos = findViewById(R.id.btSelecionarTodos)
        btSelecionarTodos.setOnClickListener {
            selecionarTodos()
        }

        // Desmarcar Todos
        val btDesmarcarTodos = findViewById<Button>(R.id.btDesmarcarTodos)
        btDesmarcarTodos.setOnClickListener {
            desmarcarTodosNoAdapter()
        }

        editTextPesquisarNome = findViewById(R.id.editTextPesquisarNome)
        editTextPesquisarData = findViewById(R.id.editTextPesquisarData)
        btPesquisarBd = findViewById(R.id.btPesquisarBd)

        btPesquisarBd.setOnClickListener {
            pesquisarNoBancoDeDados()
        }

        // Adiciona evento bt voltar
        val btVoltar = findViewById<Button>(R.id.btVoltar)
        btVoltar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Inicialize o banco de dados
        database = AppDatabaseHelper(this)

        // Configure o RecyclerView (você deve criar o layout do item da lista)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Obtenha uma instância de leitura do banco de dados
        val db = database.readableDatabase

        // Execute uma consulta no banco de dados (substitua 'Brinco' pelo nome da tabela)
        val cursor = db.rawQuery("SELECT * FROM brincos", null)

        // Crie uma lista para armazenar os dados recuperados
        val brincosList = mutableListOf<Brinco>()

        // Percorra o cursor e adicione os dados à lista
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex("id"))
            val peso = cursor.getFloat(cursor.getColumnIndex("peso"))
            val nome = cursor.getString(cursor.getColumnIndex("nome"))
            val dataHoraCadastro = cursor.getLong(cursor.getColumnIndex("dataHoraCadastro"))

            // Crie uma instância de Brinco com o "id" e adicione à lista
            val brinco = Brinco(id, peso, nome, Date(dataHoraCadastro))
            brincosList.add(brinco)
        }

        // Feche o cursor e o banco de dados
        cursor.close()
        db.close()

        // Após recuperar os dados do banco de dados, inverta a lista
        brincosList.reverse()

        // Crie um adapter personalizado para exibir os dados na RecyclerView
        val adapter = BrincosAdapter(brincosList, this) // Passe o contexto da Activity
        recyclerView.adapter = adapter
    }

    private fun selecionarTodos() {
        val adapter = recyclerView.adapter as BrincosAdapter
        val brincosList = adapter.brincosList

        if (brincosList.isNotEmpty()) {
            // Verifique se pelo menos um item está presente na lista
            // Para evitar erros ao tentar selecionar todos os itens vazios

            // Percorra a lista e selecione todos os itens
            for (brinco in brincosList) {
                adapter.setSelected(brinco.id, true)
            }

            // Notifique o adaptador sobre a mudança na seleção
            adapter.notifyDataSetChanged()
        }
    }

    private fun desmarcarTodosNoAdapter() {
        val adapter = recyclerView.adapter as BrincosAdapter
        adapter.desmarcarTodos()
    }

    private fun excluirItensSelecionados() {
        val adapter = recyclerView.adapter as BrincosAdapter
        val selectedItems = adapter.getSelectedItems()

        // Exclua os itens selecionados do banco de dados
        val db = database.writableDatabase
        for (brinco in selectedItems) {
            db.delete("brincos", "id = ?", arrayOf(brinco.id.toString()))
        }
        db.close()

        // Obtenha as posições dos itens selecionados
        val positionsToRemove = selectedItems.map { adapter.brincosList.indexOf(it) }

        // Remova os itens selecionados do adaptador usando notifyItemRemoved
        for (position in positionsToRemove) {
            adapter.brincosList.removeAt(position)
            adapter.notifyItemRemoved(position)
        }

        // Limpe a seleção no adaptador
        adapter.removeSelectedItems()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun pesquisarNoBancoDeDados() {
        // Obtenha os valores dos campos de pesquisa
        val nomePesquisa = editTextPesquisarNome.text.toString().trim()
        val dataPesquisa = editTextPesquisarData.text.toString().trim()

        // Abra o banco de dados para leitura
        val db = database.readableDatabase

        // Construa a parte da consulta SQL para a pesquisa de nome
        val nomeSelection = if (nomePesquisa.isNotEmpty()) {
            "nome LIKE ?"
        } else {
            null
        }

        // Construa a parte da consulta SQL para a pesquisa de data
        val dataSelection = if (dataPesquisa.isNotEmpty()) {
            // Modificação: Use a função strftime para comparar apenas a data (ignorando o horário)
            "strftime('%d/%m/%Y', dataHoraCadastro / 1000, 'unixepoch') = ?"
        } else {
            null
        }

        // Construa a cláusula WHERE combinando os critérios de nome e data
        val whereClause = buildWhereClause(nomeSelection, dataSelection)

        // Parâmetros para substituir nas consultas SQL
        val whereArgs = mutableListOf<String>()

        // Adicione os valores de pesquisa aos argumentos, se estiverem preenchidos
        if (nomePesquisa.isNotEmpty()) {
            whereArgs.add("%$nomePesquisa%")
        }

        if (dataPesquisa.isNotEmpty()) {
            whereArgs.add(dataPesquisa)
        }

        // Execute a consulta SQL
        val cursor = db.query("brincos", null, whereClause, whereArgs.toTypedArray(), null, null, null)

        // Crie uma lista para armazenar os resultados da pesquisa
        val resultadosPesquisa = mutableListOf<Brinco>()

        // Percorra o cursor e adicione os resultados à lista
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex("id"))
            val peso = cursor.getFloat(cursor.getColumnIndex("peso"))
            val nome = cursor.getString(cursor.getColumnIndex("nome"))
            val dataHoraCadastro = cursor.getLong(cursor.getColumnIndex("dataHoraCadastro"))

            val brinco = Brinco(id, peso, nome, Date(dataHoraCadastro))
            resultadosPesquisa.add(brinco)
        }

        // Feche o cursor e o banco de dados
        cursor.close()
        db.close()

        // Atualize o RecyclerView com os resultados da pesquisa
        val adapter = recyclerView.adapter as BrincosAdapter
        adapter.brincosList.clear()
        adapter.brincosList.addAll(resultadosPesquisa)
        adapter.notifyDataSetChanged()
    }

    private fun buildWhereClause(vararg conditions: String?): String {
        val filteredConditions = conditions.filterNotNull()
        return if (filteredConditions.isNotEmpty()) {
            filteredConditions.joinToString(" AND ")
        } else {
            null
        } ?: ""
    }
}