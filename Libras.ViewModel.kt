package com.example.librasapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class LibrasUiState(
    val inputText: String = "",
    val translationResult: String = "",
    val detectedLanguage: String = "",
    val isLoading: Boolean = false
)

class LibrasViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LibrasUiState())
    val uiState: StateFlow<LibrasUiState> = _uiState.asStateFlow()

    private val languageIdentifier: LanguageIdentifier = LanguageIdentification.getClient()

    // Dicionário básico de palavras comuns para demonstração
    private val librasTranslations = mapOf(
        "oi" to "👋 [Mão aberta balançando de um lado para o outro]",
        "olá" to "👋 [Mão aberta balançando de um lado para o outro]",
        "tchau" to "👋 [Mão aberta abrindo e fechando]",
        "obrigado" to "🙏 [Mãos juntas próximas ao peito]",
        "obrigada" to "🙏 [Mãos juntas próximas ao peito]",
        "por favor" to "🤲 [Mãos abertas para cima, movimento para frente]",
        "desculpa" to "✋ [Mão na testa, movimento circular]",
        "desculpe" to "✋ [Mão na testa, movimento circular]",
        "sim" to "👍 [Punho fechado com polegar para cima]",
        "não" to "☝️ [Dedo indicador balançando horizontalmente]",
        "bom dia" to "☀️👋 [Sol + cumprimento com mão]",
        "boa tarde" to "🌤️👋 [Sol parcial + cumprimento]",
        "boa noite" to "🌙👋 [Lua + cumprimento]",
        "como vai" to "👋❓ [Cumprimento + pergunta com expressão facial]",
        "tudo bem" to "👍✌️ [Polegar + sinal de OK]",
        "água" to "💧 [Movimento de beber com mão em formato de copo]",
        "comida" to "🍽️ [Movimento de levar comida à boca]",
        "casa" to "🏠 [Mãos formando telhado triangular]",
        "família" to "👨‍👩‍👧‍👦 [Círculo com pessoas próximas]",
        "trabalho" to "💼 [Mãos simulando atividade repetitiva]",
        "escola" to "🏫 [Mãos abertas simulando um livro]",
        "amor" to "❤️ [Mãos sobre o coração]",
        "amigo" to "🤝 [Aperto de mãos]",
        "amiga" to "🤝 [Aperto de mãos]",
        "feliz" to "😊 [Sorriso com mãos indicando alegria]",
        "triste" to "😢 [Dedos escorrendo pelo rosto como lágrimas]",
        "nome" to "✍️ [Apontar para si mesmo]",
        "idade" to "🔢 [Mostrar números com os dedos]",
        "pai" to "👨 [Mão na testa, movimento para baixo]",
        "mãe" to "👩 [Mão no queixo, movimento para baixo]",
        "filho" to "👶 [Gesto de embalar]",
        "filha" to "👶 [Gesto de embalar]",
        "irmão" to "👬 [Dois dedos juntos]",
        "irmã" to "👭 [Dois dedos juntos]",
        "avô" to "👴 [Mão na testa, barba]",
        "avó" to "👵 [Mão no queixo, cabelos]",
        "hello" to "👋 [Wave hand gesture]",
        "hi" to "👋 [Wave hand gesture]",
        "thank you" to "🙏 [Hands together near chest]",
        "thanks" to "🙏 [Hands together near chest]",
        "yes" to "👍 [Thumbs up]",
        "no" to "☝️ [Index finger shaking horizontally]",
        "good morning" to "☀️👋 [Sun + greeting]",
        "good afternoon" to "🌤️👋 [Partial sun + greeting]",
        "good night" to "🌙👋 [Moon + greeting]",
        "how are you" to "👋❓ [Greeting + question with facial expression]"
    )

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun translateToLibras() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Detectar idioma usando ML Kit
            detectLanguage(_uiState.value.inputText)

            // Simular processamento
            delay(2000)

            // Traduzir para Libras
            val translation = translateText(_uiState.value.inputText)

            _uiState.value = _uiState.value.copy(
                translationResult = translation,
                isLoading = false
            )
        }
    }

    private fun detectLanguage(text: String) {
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                val languageName = when (languageCode) {
                    "pt" -> "Português"
                    "en" -> "Inglês"
                    "es" -> "Espanhol"
                    "fr" -> "Francês"
                    "de" -> "Alemão"
                    "it" -> "Italiano"
                    "ja" -> "Japonês"
                    "ko" -> "Coreano"
                    "zh" -> "Chinês"
                    "ar" -> "Árabe"
                    "ru" -> "Russo"
                    "und" -> "Não identificado"
                    else -> languageCode.uppercase()
                }

                _uiState.value = _uiState.value.copy(detectedLanguage = languageName)

                // Aviso se não for português
                if (languageCode != "pt") {
                    val currentTranslation = translateText(_uiState.value.inputText)
                    _uiState.value = _uiState.value.copy(
                        translationResult = "⚠️ AVISO: Texto em $languageName detectado!\n\n" +
                                "Para melhor precisão na tradução para LIBRAS (Língua Brasileira de Sinais), " +
                                "recomendamos usar textos em português.\n\n" +
                                "Tradução disponível:\n\n$currentTranslation"
                    )
                }
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(detectedLanguage = "Erro na detecção de idioma")
            }
    }

    private fun translateText(text: String): String {
        val words = text.lowercase()
            .replace(Regex("[.,!?;:]"), "")
            .split(" ")
            .filter { it.isNotBlank() }
        val translations = mutableListOf<String>()
        val foundWords = mutableSetOf<String>()

        // Verificar frases completas primeiro
        val fullText = words.joinToString(" ")
        for ((phrase, translation) in librasTranslations) {
            if (fullText.contains(phrase) && !foundWords.contains(phrase)) {
                translations.add("• \"$phrase\" → $translation")
                foundWords.add(phrase)
            }
        }

        // Depois verificar palavras individuais
        for (word in words) {
            val cleanWord = word.trim()
            if (cleanWord.isNotEmpty() && librasTranslations.containsKey(cleanWord) && !foundWords.contains(cleanWord)) {
                translations.add("• \"$cleanWord\" → ${librasTranslations[cleanWord]}")
                foundWords.add(cleanWord)
            }
        }

        return if (translations.isNotEmpty()) {
            "✅ SINAIS ENCONTRADOS:\n\n" +
                    translations.joinToString("\n\n") +
                    "\n\n💡 DICA IMPORTANTE - Estrutura em LIBRAS:\n" +
                    "A ordem das palavras em LIBRAS é diferente do português!\n" +
                    "Estrutura: TEMPO + SUJEITO + OBJETO + VERBO\n\n" +
                    "📝 EXEMPLO:\n" +
                    "Português: 'Eu vou trabalhar amanhã'\n" +
                    "LIBRAS: 'AMANHÃ EU TRABALHO IR'\n\n" +
                    "🤲 Use também expressões faciais para complementar os sinais!"
        } else {
            "❌ PALAVRAS NÃO ENCONTRADAS no dicionário atual.\n\n" +
                    "📝 TEXTO DIGITADO: \"$text\"\n\n" +
                    "💡 SUGESTÕES:\n" +
                    "Tente palavras básicas como:\n" +
                    "• Cumprimentos: oi, olá, tchau, bom dia\n" +
                    "• Cortesia: obrigado, por favor, desculpe\n" +
                    "• Respostas: sim, não, tudo bem\n" +
                    "• Família: pai, mãe, filho, irmão\n" +
                    "• Lugares: casa, escola, trabalho\n" +
                    "• Sentimentos: feliz, triste, amor\n\n" +
                    "🔤 DATILOLOGIA (Alfabeto Manual):\n" +
                    "Para palavras não encontradas, use o alfabeto manual:\n" +
                    "A=✊ B=🤚 C=☝️ D=👌 E=✋ F=👍 G=👆 H=✌️ I=🤞 J=🤙\n" +
                    "K=🤘 L=🤟 M=👊 N=👎 O=👌 P=👇 Q=☝️ R=✌️ S=✊ T=👍\n" +
                    "U=✌️ V=✌️ W=🤟 X=☝️ Y=🤙 Z=☝️"
        }
    }

    fun clearText() {
        _uiState.value = LibrasUiState()
    }

    override fun onCleared() {
        super.onCleared()
        languageIdentifier.close()
    }
}
