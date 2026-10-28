package com.example.toxicdetector

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    private lateinit var etComment: EditText
    private lateinit var btnAnalyze: Button
    private lateinit var btnClear: Button
    private lateinit var resultCard: CardView
    private lateinit var tvEmoji: TextView
    private lateinit var tvVerdict: TextView
    private lateinit var tvConfidence: TextView
    private lateinit var progressBar: ProgressBar

    // Список токсичных слов (вес 0.35)
    private val toxicWords = setOf(
        "идиот", "дурак", "тупой", "кретин", "придурок", "мерзавец",
        "ненавижу", "убью", "умри", "провались", "скотина", "мразь",
        "idiot", "stupid", "hate", "kill", "die", "moron", "loser",
        "ugly", "worthless", "trash", "garbage", "dumb", "shut up",
        "ублюдок", "урод", "козёл", "сволочь", "негодяй"
    )

    // Список умеренно токсичных слов (вес 0.15)
    private val moderateWords = setOf(
        "плохой", "ужасный", "отстой", "бесполезный", "разочарование",
        "bad", "awful", "terrible", "boring", "useless", "disappointing",
        "horrible", "worst", "annoying", "fail"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация View
        etComment = findViewById(R.id.etComment)
        btnAnalyze = findViewById(R.id.btnAnalyze)
        btnClear = findViewById(R.id.btnClear)
        resultCard = findViewById(R.id.resultCard)
        tvEmoji = findViewById(R.id.tvEmoji)
        tvVerdict = findViewById(R.id.tvVerdict)
        tvConfidence = findViewById(R.id.tvConfidence)
        progressBar = findViewById(R.id.progressBar)

        // Кнопка "Проверить"
        btnAnalyze.setOnClickListener {
            val text = etComment.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Введите текст для анализа", Toast.LENGTH_SHORT).show()
            } else {
                analyzeText(text)
            }
        }

        // Кнопка "Очистить"
        btnClear.setOnClickListener {
            etComment.text.clear()
            resultCard.visibility = View.GONE
        }
    }

    private fun analyzeText(text: String) {
        val score = classifyWithRules(text)
        showResult(score)
    }

    // Алгоритм оценки токсичности
    private fun classifyWithRules(text: String): Float {
        val lower = text.lowercase()

        var toxicCount = 0
        var moderateCount = 0

        // Считаем вхождения слов (используем contains, чтобы ловить формы слов)
        for (word in toxicWords) {
            if (lower.contains(word)) toxicCount++
        }
        for (word in moderateWords) {
            if (lower.contains(word)) moderateCount++
        }

        // Структурный анализ
        val exclamations = text.count { it == '!' }
        val caps = text.count { it.isUpperCase() }.toFloat()
        val capsRatio = if (text.isNotEmpty()) caps / text.length else 0f

        // Итоговая формула
        var score = 0f
        score += toxicCount * 0.35f
        score += moderateCount * 0.15f
        score += (exclamations * 0.05f).coerceAtMost(0.15f) // Не более 0.15 за восклицания
        score += (capsRatio * 0.2f)

        // Ограничиваем от 0 до 1
        return score.coerceIn(0f, 1f)
    }

    // Отображение результата в UI
    private fun showResult(toxicScore: Float) {
        resultCard.visibility = View.VISIBLE
        val percent = (toxicScore * 100).toInt()
        progressBar.progress = percent

        when {
            // Токсично (>= 30%)
            toxicScore >= 0.3f -> {
                tvEmoji.text = "☠️"
                tvVerdict.text = "ТОКСИЧНО"
                tvVerdict.setTextColor(Color.parseColor("#D32F2F"))
                tvConfidence.text = "Уверенность: $percent% (Анализ слов)"
                progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#D32F2F"))
            }
            // Подозрительно (>= 15%)
            toxicScore >= 0.15f -> {
                tvEmoji.text = "⚠️"
                tvVerdict.text = "ПОДОЗРИТЕЛЬНО"
                tvVerdict.setTextColor(Color.parseColor("#F57C00"))
                tvConfidence.text = "Уверенность: $percent% (Анализ слов)"
                progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F57C00"))
            }
            // Нетоксично (< 15%)
            else -> {
                tvEmoji.text = "✅"
                tvVerdict.text = "НЕТОКСИЧНО"
                tvVerdict.setTextColor(Color.parseColor("#388E3C"))
                tvConfidence.text = "Уверенность: ${100 - percent}% чисто (Анализ слов)"
                progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#388E3C"))
            }
        }
    }
}