package com.zachklipp.richtext.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.string.RichTextRenderOptions
import kotlinx.coroutines.delay
import kotlin.random.Random

@Preview
@Composable private fun AnimatedRichTextSamplePreview() {
  AnimatedRichTextSample()
}

@Preview
@Composable private fun ChineseAnimatedRichTextSamplePreview() {
  ChineseAnimatedRichTextSample()
}

@Preview
@Composable private fun ThaiAnimatedRichTextSamplePreview() {
  ThaiAnimatedRichTextSample()
}

@Preview
@Composable private fun HindiAnimatedRichTextSamplePreview() {
  HindiAnimatedRichTextSample()
}

@Composable fun AnimatedRichTextSample() {
  AnimatedTextWrapper(
    title = "Stream incrementally",
    completeContent = { CompleteTextSample() },
    chunkedContent = { ChunkingTextSample() },
  )
}

@Composable fun ChineseAnimatedRichTextSample() {
  AnimatedTextWrapper(
    title = "Stream incrementally",
    completeContent = { ChineseCompleteTextSample() },
    chunkedContent = { ChineseChunkingTextSample() },
  )
}

@Composable fun ThaiAnimatedRichTextSample() {
  AnimatedTextWrapper(
    title = "Stream incrementally",
    completeContent = { ThaiCompleteTextSample() },
    chunkedContent = { ThaiChunkingTextSample() },
  )
}

@Composable fun HindiAnimatedRichTextSample() {
  AnimatedTextWrapper(
    title = "Stream incrementally",
    completeContent = { HindiCompleteTextSample() },
    chunkedContent = { HindiChunkingTextSample() },
  )
}

@Composable
private fun AnimatedTextWrapper(
  title: String,
  completeContent: @Composable () -> Unit,
  chunkedContent: @Composable () -> Unit,
) {
  var isChunked by remember { mutableStateOf(false) }

  SampleTheme {
    Surface {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            title,
            modifier = Modifier
              .weight(1f)
              .padding(16.dp),
          )
          Checkbox(isChunked, onCheckedChange = { isChunked = it })
        }
        Box(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
          if (!isChunked) {
            completeContent()
          } else {
            chunkedContent()
          }
        }
      }
    }
  }
}

@Composable
private fun CompleteTextSample() {
  val markdownOptions = remember {
    RichTextRenderOptions(
      animate = true,
      textFadeInMs = 500,
      delayMs = 70,
      debounceMs = 200,
    )
  }

  RichText {
    Markdown(
      SampleText,
      richtextRenderOptions = markdownOptions,
    )
  }
}

@Composable
private fun ChunkingTextSample() {
  var currentText by remember { mutableStateOf("") }
  var isComplete by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    var remaining = SampleText
    while (remaining.isNotEmpty()) {
      delay(200L + Random.nextInt(500))
      val chunkLength = 10 + Random.nextInt(100)
      currentText += remaining.take(chunkLength)
      remaining = remaining.drop(chunkLength)
    }
    isComplete = true
  }

  val markdownOptions = remember(isComplete) {
    RichTextRenderOptions(
      animate = !isComplete,
      textFadeInMs = 500,
      delayMs = 70,
      debounceMs = 200,
    )
  }

  RichText {
    Markdown(
      currentText,
      richtextRenderOptions = markdownOptions,
    )
  }
}

@Composable
private fun ChineseCompleteTextSample() {
  val markdownOptions = remember {
    RichTextRenderOptions(
      animate = true,
      textFadeInMs = 500,
      delayMs = 70,
      debounceMs = 200,
    )
  }

  RichText {
    Markdown(
      ChineseSampleText,
      richtextRenderOptions = markdownOptions,
    )
  }
}

@Composable
private fun ChineseChunkingTextSample() {
  var currentText by remember { mutableStateOf("") }
  var isComplete by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    var remaining = ChineseSampleText
    while (remaining.isNotEmpty()) {
      delay(200L + Random.nextInt(500))
      val chunkLength = 6 + Random.nextInt(40)
      currentText += remaining.take(chunkLength)
      remaining = remaining.drop(chunkLength)
    }
    isComplete = true
  }

  val markdownOptions = remember(isComplete) {
    RichTextRenderOptions(
      animate = !isComplete,
      textFadeInMs = 500,
      delayMs = 70,
      debounceMs = 200,
    )
  }

  RichText {
    Markdown(
      currentText,
      richtextRenderOptions = markdownOptions,
    )
  }
}

@Composable
private fun ThaiCompleteTextSample() {
  val markdownOptions = remember {
    RichTextRenderOptions(
      animate = true,
      textFadeInMs = 500,
      delayMs = 70,
      debounceMs = 200,
    )
  }

  RichText {
    Markdown(
      ThaiSampleText,
      richtextRenderOptions = markdownOptions,
    )
  }
}

@Composable
private fun ThaiChunkingTextSample() {
  var currentText by remember { mutableStateOf("") }
  var isComplete by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    var remaining = ThaiSampleText
    while (remaining.isNotEmpty()) {
      delay(200L + Random.nextInt(500))
      val chunkLength = 6 + Random.nextInt(40)
      currentText += remaining.take(chunkLength)
      remaining = remaining.drop(chunkLength)
    }
    isComplete = true
  }

  val markdownOptions = remember(isComplete) {
    RichTextRenderOptions(
      animate = !isComplete,
      textFadeInMs = 500,
      delayMs = 70,
      debounceMs = 200,
    )
  }

  RichText {
    Markdown(
      currentText,
      richtextRenderOptions = markdownOptions,
    )
  }
}

@Composable
private fun HindiCompleteTextSample() {
  val markdownOptions = remember {
    RichTextRenderOptions(
      animate = true,
      textFadeInMs = 500,
      delayMs = 70,
      debounceMs = 200,
    )
  }

  RichText {
    Markdown(
      HindiSampleText,
      richtextRenderOptions = markdownOptions,
    )
  }
}

@Composable
private fun HindiChunkingTextSample() {
  var currentText by remember { mutableStateOf("") }
  var isComplete by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    var remaining = HindiSampleText
    while (remaining.isNotEmpty()) {
      delay(200L + Random.nextInt(500))
      val chunkLength = 6 + Random.nextInt(40)
      currentText += remaining.take(chunkLength)
      remaining = remaining.drop(chunkLength)
    }
    isComplete = true
  }

  val markdownOptions = remember(isComplete) {
    RichTextRenderOptions(
      animate = !isComplete,
      textFadeInMs = 500,
      delayMs = 70,
      debounceMs = 200,
    )
  }

  RichText {
    Markdown(
      currentText,
      richtextRenderOptions = markdownOptions,
    )
  }
}

private const val SampleText = """
1-The quick brown fox jumps over the lazy dog.
1-The quick brown fox jumps over the lazy dog.
1-The quick brown fox jumps over the lazy dog.
1-The quick brown fox jumps over the lazy dog.
1-The quick brown fox jumps over the lazy dog.

* Formatted list 1
* Formatted list 2
  * Sub bullet point
* Formatted list 3
* Formatted list 4 but with some very long text
* **This is a title** And then a lot more text and maybe a story and a much longer block of text and hello there this is a test
* 7
* 8
* 9
* 10



# Header 1
2-The quick brown fox jumps over the lazy dog.
The quick brown fox jumps over the lazy dog.

| Column A | Column B |
|----------|----------|
| The quick brown fox jumps over the lazy dog. | The quick brown fox jumps over the lazy dog. |

##### Header 5
4-The quick brown fox jumps over the lazy dog.
The quick brown fox jumps over the lazy dog.
The quick brown fox jumps over the lazy dog.
The quick brown fox **jumps over the lazy dog.**
"""

private const val ChineseSampleText = """
# 大段落和列表交替

第一段没有使用任何标点符号我們不断地写下去仿佛一口气都不愿意停下来只为了测试在长中文文本中动画何时开始生效長長久久的文字连贯地排列直到最终换行
这一段继续延伸依旧没有任何停顿让我们能够观察在无标点的情况下动画如何处理这些词句因为这在列表和段落中很常见
这是第一段的尾声依然没有句号因為我們希望強迫整段文本被視為一個超長詞組

* **粗体列表一** 这一行用于测试在列表里加粗文本而且没有句号
* **粗体列表二** 这行同样没有标点并且包含更多的文字来延长动画
* **粗体列表三** 包含很多很多的文字一直延伸最终在这里结束
  * **粗体子项目** 子项同样保持没有标点以便检查子级列表

第二段文字是一个比较长的段落它包含中文句号和逗号，但是也混杂了没有标点的句子让我们看看动画能否正常结束。还有一行没有标点用来测试整段结束后的行为
继续在第二段里添加更多文字以确保段落足够长让动画不会过早结束同样我们在中途不会停顿

1. *斜体样式项目一* 混合`代码片段`以及[链接](https://example.com)展示不同的风格
2. ~~删除线项目二~~ 包含**粗体**和*斜体*并且没有句号
3. 混合样式项目三 **加粗** *斜体* `代码` [链接](https://example.com) 持续扩展这个句子直到没有标点为止
   1. 子项目三之一 **子项中的粗体** 也可能没有标点为了继续测试
   2. 子项目三之二 包含`代码`和*斜体*同时缺乏终止符

第三段再次变得非常长我们不停地往后添加文字没有任何停顿这对于动画来说是一个极端的测试因为整段看起来像一个巨大的短语但实际上我们只是想确保在没有标点时段落也能完整显示出来
我们继续书写这一段广袤的文字内容不添加标点直到最后一刻让整段维持一种悬而未决的状态

- 列表风格一 **粗体** *斜体* 汇集在同一行里以增加复杂度
- 列表风格二 `代码` [链接](https://example.com) 再加上~~删除线~~来观察不同样式叠加
- 列表风格三 没有标点没有停顿一直延续延续延续延续延续延续延续直到最后才让它自然结束

第四段告诉我们下面即将出现代码块和表格因此我们努力延长段落长度来观察长文本结合代码块时的动画表现最终我们在这里停顿。
```kotlin
fun greet(name: String) {
    println("你好，jo")
}
```

| 列A | 列B |
|-----|-----|
| 内容A | 内容B |
| 很长的一段内容A | 更长的内容B 用来测试表格展示 |

> 最后再来一个引用块其中包含 **粗体** 和 *倾斜* 以及 `代码` 继续延长直到我们觉得足够
> 引用的第二行也不含标点我们想观察整个引用是否可以被完整显示

结尾的大段落再次没有标点我们仍然不断添加文本直到最后在这里加上句号。
"""

private const val ThaiSampleText = """
# ย่อหน้าใหญ่และรายการสลับ

ย่อหน้าแรกไม่มีการใช้วรรคตอนเราพิมพ์อย่างต่อเนื่องเพื่อทดสอบการแบ่งวลีในภาษาไทยซึ่งไม่มีช่องว่างระหว่างคำและเรายังอยากรู้ว่าเอฟเฟกต์จะทำงานอย่างไรเมื่อข้อมูลมาถึงทีละน้อยๆและยังคงยืดยาวต่อไปจนกว่าเราจะเลือกขึ้นบรรทัดใหม่
ย่อหน้านี้ยังไม่หยุดเพราะเราต้องการสร้างสถานการณ์ที่ข้อความยาวมากๆโดยยังไม่มีจุดสิ้นสุดอย่างชัดเจนเพื่อดูว่าการเรนเดอร์จะยังคงทำงานลื่นไหลหรือไม่
ตอนท้ายของย่อหน้าจึงยังคงไม่มีจุดเพื่อให้มันดูเหมือนวลีที่ไม่มีวันจบ

* **รายการตัวหนา ๑** ใช้ทดสอบการแสดงผลของรายการพร้อมตัวหนาและไม่มีวรรคตอน
* **รายการตัวหนา ๒** ยังคงต่อเนื่องโดยไม่มีจุดและเพิ่มความยาวของประโยค
* **รายการตัวหนา ๓** ยาวมากแบบไม่มีการหยุดพักเพื่อดูว่ามันจะถูกตัดอย่างไร
  * **รายการย่อยตัวหนา** รายการในระดับย่อยก็ยังไม่มีวรรคตอนเหมือนกัน

ย่อหน้าที่สองเพิ่มทั้งประโยคที่มีวรรคตอนและไม่มีวรรคตอนเพื่อผสมผสานทั้งสองรูปแบบให้เห็นความแตกต่างของการตัดข้อความ ยังมีบรรทัดหนึ่งที่ไม่มีจุดเพื่อดูว่าการค้างอยู่ตรงท้ายจะอัพเดตหรือไม่
เรายังคงเขียนยาวไปเรื่อยๆโดยไม่มีช่วงหยุดเพื่อให้ย่อหน้านี้นานมากและดูว่าค่า debounce จะมีผลแค่ไหน

1. *รายการเอียง* รวม`โค้ดสั้นๆ`และ[ลิงก์](https://example.com)ไว้ในบรรทัดเดียว
2. ~~รายการขีดฆ่า~~ รวมทั้ง**ตัวหนา**และ*ตัวเอียง*พร้อมกับข้อความยาวที่ยังไม่มีจุด
3. รายการผสม **ตัวหนา** *เอียง* `โค้ด` [ลิงก์](https://example.com) และไม่มีวรรคตอนเพื่อทดสอบการเคลื่อนไหว
   1. รายการย่อยผสม **ตัวหนา** ที่ไม่มีวรรคตอน
   2. รายการย่อยผสมที่สองมี`โค้ด`และ*เอียง*และยังคงไม่มีจุดท้ายบรรทัด

ย่อหน้าที่สามยังยาวมากเพื่อให้เห็นการเรนเดอร์ต่อเนื่องเราจะไม่ใส่ช่องว่างหรือจุดจบใดๆจนกว่าจะถึงตอนท้ายสุดเพื่อทำให้เกิดสถานการณ์ที่ยาวที่สุดเท่าที่จะเป็นไปได้
เราใส่ข้อความอีกหลายประโยคติดกันเพื่อให้มันยาวมากขึ้นและยังไม่มีการเว้นวรรคหรือเครื่องหมายใดๆเพื่อเน้นปัญหาที่อาจเกิดขึ้น

- รายการสไตล์หนึ่ง **ตัวหนา** *เอียง* อยู่ในบรรทัดเดียวเพื่อให้การเรนเดอร์ซับซ้อนขึ้น
- รายการสไตล์สอง `โค้ด` [ลิงก์](https://example.com) และ~~ขีดฆ่า~~รวมกันในบรรทัดเดียว
- รายการสไตล์สาม ไม่มีวรรคตอนและยืดยาวมากเพื่อดูว่าการแบ่งวลีทำงานหรือไม่

ย่อหน้าสุดท้ายก่อนโค้ดบอกว่าด้านล่างจะมีบล็อกโค้ดและตารางแล้วเราก็พิมพ์ยาวอีกเล็กน้อยก่อนจะหยุด
```kotlin
fun greet(name: String) {
    println("สวัสดี name")
}
```

| คอลัมน์ก | คอลัมน์ข |
|-----------|-----------|
| เนื้อหาก | เนื้อหาข |
| เนื้อหาที่ยาวมากกก | เนื้อหาที่ยาวกว่ายาวเพื่อทดสอบตาราง |

> ปิดท้ายด้วยบล็อกอ้างอิงที่มี **ตัวหนา** *ตัวเอียง* และ `โค้ด` ต่อเนื่องไปอีกนิดเพื่อให้มันยาวพอ
> บรรทัดต่อจากนั้นของอ้างอิงก็ยังไม่มีวรรคตอนเพื่อดูว่าจะแสดงครบหรือไม่

ประโยคสุดท้ายของไฟล์ตัวอย่างนี้จะมีจุดอยู่ตรงนี้.
"""

private const val HindiSampleText = """
# लंबे परिच्छेद और सूचियाँ

पहला परिच्छेद बिना किसी विरामचिह्न के लगातार चलता रहता हैहम बिना रुके लगातार लिखते जा रहे हैं ताकि देखा जा सके कि हिंदी में जहां शब्दों के बीच अक्सर स्पेस नहीं होता है वहां एनीमेशन किस तरह प्रतिक्रिया करता हैऔर हम निर्णय लेने तक इसे इसी तरह बढ़ाते रहते हैं
दूसरा वाक्य भी बिना किसी पूर्ण विराम के जारी है जिससे यह एक लंबा वाक्य बनता जाए और एनीमेशन को मजबूर करे कि वह इसे एक ही वाक्यांश की तरह संभाले
यह अंत तक बिना विराम के चलता है ताकि हम देख सकें कि अंतिम हिस्से को कैसे प्रस्तुत किया जाएगा

* **मोटे अक्षरों वाला आइटम १** यह पंक्ति सूची में बोल्ड टेक्स्ट के साथ है और कोई विरामचिह्न नहीं है
* **मोटे अक्षरों वाला आइटम २** यहाँ भी पूर्ण विराम नहीं है और वाक्य को लंबा किया गया है
* **मोटे अक्षरों वाला आइटम ३** बहुत लंबा वाक्य है जो अंत तक किसी विराम के बिना जारी रहता है
  * **मोटा उप-आइटम** उप सूची भी बिना विराम के रहती है ताकि नेस्टिंग का परीक्षण हो सके

दूसरे परिच्छेद में हम कुछ वाक्यों में पूर्ण विराम जोड़ते हैं, कुछ में नहीं, ताकि दोनों स्थितियों को एक साथ देखा जा सके। अंत में एक पंक्ति बिना विराम के रखी गई है ताकि पता चले कि वह कब रेंडर होती है
हम इस पैराग्राफ को भी लंबा करते जाते हैं ताकि यह सुनिश्चित कर सकें कि डिले के दौरान भी सब कुछ ठीक काम करे

1. *तिरछा आइटम* जिसमें `कोड` और [लिंक](https://example.com) एक ही पंक्ति में हों
2. ~~कटा हुआ आइटम~~ जिसमें **मोटे अक्षर** और *तिरछे अक्षर* दोनों हैं और कोई पूर्ण विराम नहीं है
3. मिश्रित आइटम **मोटा** *तिरछा* `कोड` [लिंक](https://example.com) सब कुछ शामिल करता है और फिर भी वाक्य समाप्त नहीं करता
   1. उप आइटम एक **मोटे अक्षर** वाला और बिना विराम के
   2. उप आइटम दो जिसमें `कोड` और *तिरछे अक्षर* हैं और फिर भी यह लंबा चलता है

तीसरा परिच्छेद फिर से बहुत लंबा है हम बिना रुके लिखते चले जाते हैं ताकि यह देखा जा सके कि बिना किसी विरामचिह्न के लंबे हिस्से के साथ एनीमेशन कैसा काम करता है और कब वह समाप्ति मानता है
हम और भी शब्द जोड़ते जाते हैं ताकि पैराग्राफ भारी हो जाए लेकिन अभी भी कोई पूर्ण विराम या अन्य चिन्ह नहीं लगाया जाता

- सूची शैली एक **बोल्ड** *तिरछा* और अन्य सजावट एक ही पंक्ति में रखती है
- सूची शैली दो `कोड` [लिंक](https://example.com) और~~कटा हुआ~~ को मिलाकर लंबी पंक्ति बनाती है
- सूची शैली तीन बिना विराम के लगातार विस्तार करती जाती है ताकि देखें कि विभाजन कहाँ होता है

इसके बाद हम कोड ब्लॉक और तालिका दिखाने से पहले एक और लंबा वाक्य लिखते हैं ताकि संदर्भ पूरा हो
```kotlin
fun greet(name: String) {
    println("नमस्ते name")
}
```

| स्तम्भ क | स्तम्भ ख |
|----------|----------|
| सामग्री क | सामग्री ख |
| बहुत लंबी सामग्री | उससे भी लंबी सामग्री जो तालिका की चौड़ाई की परीक्षा ले |

> अंत में एक उद्धरण ब्लॉक है जिसमें **मोटे अक्षर** *तिरछे अक्षर* और `कोड` शामिल है और इसे थोड़ा लंबा किया गया है ताकि प्रभाव स्पष्ट हो
> उद्धरण की अगली पंक्ति में भी कोई विरामचिह्न नहीं है ताकि यह देखा जा सके कि वह अंत तक प्रदर्शित होती है या नहीं

अंतिम वाक्य आखिरकार यहाँ समाप्त हो जाता है।
"""
