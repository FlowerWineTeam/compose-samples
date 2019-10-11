/*
 * Copyright 2019 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jetnews.ui

import androidx.compose.Composable
import androidx.compose.composer
import androidx.compose.unaryPlus
import androidx.ui.core.Clip
import androidx.ui.core.Dp
import androidx.ui.core.Px
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.DrawShape
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.graphics.vector.compat.vectorResource
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.FlexRow
import androidx.ui.layout.HeightSpacer
import androidx.ui.layout.Padding
import androidx.ui.layout.Row
import androidx.ui.layout.WidthSpacer
import androidx.ui.material.surface.Surface
import androidx.ui.material.themeTextStyle
import androidx.ui.material.withOpacity
import androidx.ui.text.AnnotatedString
import androidx.ui.text.ParagraphStyle
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.text.style.TextDecoration
import androidx.ui.text.style.TextIndent
import com.example.jetnews.R

private const val defaultBodyLineHeight = 1.49f
private val defaultSpacerSize = 16.dp

private val italicTextStyle = (+themeTextStyle { body1 }).copy(fontStyle = FontStyle.Italic)
private val boldTextStyle = (+themeTextStyle { body1 }).copy(fontWeight = FontWeight.Bold)
private val codeBlockBackground = Color(0xfff1f1f1.toInt())
private val codeTextStyle = (+themeTextStyle { body1 })
    .copy(background = codeBlockBackground, fontFamily = FontFamily.Monospace)
private val linkTextStyle = (+themeTextStyle { body1 }).copy(decoration = TextDecoration.Underline)

private val bulletParagraphStyle = ParagraphStyle(textIndent = TextIndent(firstLine = Px(30f)))

@Composable
fun PostContent(post: Post) {
    VerticalScroller {
        Padding(left = defaultSpacerSize, right = defaultSpacerSize) {
            Column {
                HeightSpacer(height = defaultSpacerSize)
                PostHeaderImage(post)
                Text(text = post.title, style = +themeTextStyle { h4 })
                HeightSpacer(height = 8.dp)
                post.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = (+themeTextStyle { body2 }).withOpacity(0.6f),
                        paragraphStyle = ParagraphStyle(lineHeight = 1.428f) // 20sp line height
                    )
                    HeightSpacer(height = defaultSpacerSize)
                }
                PostMetadata(metadata = post.metadata)
                HeightSpacer(height = 24.dp)
                PostContents(paragraphs = post.paragraphs)
                HeightSpacer(height = 48.dp)
            }
        }
    }
}

@Composable
private fun PostHeaderImage(post: Post) {
    post.image?.let {
        Container(expanded = true, height = 180.dp) {
            Clip(shape = RoundedCornerShape(4.dp)) {
                DrawImage(it)
            }
        }
        HeightSpacer(height = defaultSpacerSize)
    }
}

@Composable
private fun PostMetadata(metadata: Metadata) {
    Row {
        DefaultAuthorImage()
        WidthSpacer(width = 8.dp)
        Column {
            HeightSpacer(4.dp)
            Text(
                text = metadata.author.name,
                style = (+themeTextStyle { caption }).withOpacity(0.87f)
            )
            Text(
                text = "${metadata.date} • ${metadata.readTimeMinutes} min read",
                style = (+themeTextStyle { caption }).withOpacity(0.6f)
            )
        }
    }
}

@Composable
private fun DefaultAuthorImage() {
    val defaultImage = +vectorResource(R.drawable.ic_account_circle_black)
    Container(width = 40.dp, height = 40.dp) {
        DrawVector(vectorImage = defaultImage)
    }
}

@Composable
private fun PostContents(paragraphs: List<Paragraph>) {
    paragraphs.forEach {
        Paragraph(paragraph = it)
    }
}

@Composable
private fun Paragraph(paragraph: Paragraph) {
    val (textStyle, paragraphStyle, trailingPadding) = paragraph.type.getTextAndParagraphStyle()

    val annotatedString = paragraphToAnnotatedString(paragraph)
    Padding(bottom = trailingPadding) {
        when (paragraph.type) {
            ParagraphType.Bullet -> BulletParagraph(
                text = annotatedString,
                textStyle = textStyle,
                paragraphStyle = paragraphStyle
            )
            ParagraphType.CodeBlock -> CodeBlockParagraph(
                text = annotatedString,
                textStyle = textStyle,
                paragraphStyle = paragraphStyle
            )
            ParagraphType.Header -> {
                Padding(top = 16.dp) {
                    Text(
                        text = annotatedString,
                        style = textStyle,
                        paragraphStyle = paragraphStyle
                    )
                }
            }
            else -> Text(
                text = annotatedString,
                style = textStyle,
                paragraphStyle = paragraphStyle
            )
        }
    }
}

@Composable
private fun CodeBlockParagraph(
    text: AnnotatedString,
    textStyle: TextStyle,
    paragraphStyle: ParagraphStyle
) {
    Surface(
        color = codeBlockBackground,
        shape = RoundedCornerShape(4.dp)
    ) {
        Padding(16.dp) {
            Text(
                text = text,
                style = textStyle,
                paragraphStyle = paragraphStyle
            )
        }
    }
}

@Composable
private fun BulletParagraph(
    text: AnnotatedString,
    textStyle: TextStyle,
    paragraphStyle: ParagraphStyle
) {
    FlexRow {
        inflexible {
            Container(width = 8.dp, height = 8.dp) {
                DrawShape(shape = CircleShape, color = Color.DarkGray)
            }
        }
        flexible(flex = 1f) {
            Text(text = text, style = textStyle, paragraphStyle = paragraphStyle)
        }
    }
}

private data class ParagraphStyling(
    val textStyle: TextStyle,
    val paragraphStyle: ParagraphStyle,
    val trailingPadding: Dp
)

private fun ParagraphType.getTextAndParagraphStyle(): ParagraphStyling {
    var textStyle: TextStyle = +themeTextStyle { body1 }
    var paragraphStyle = ParagraphStyle()
    var trailingPadding = 24.dp

    when (this) {
        ParagraphType.Caption -> textStyle = +themeTextStyle { body1 }
        ParagraphType.Title -> textStyle = +themeTextStyle { h4 }
        ParagraphType.Subhead -> {
            textStyle = +themeTextStyle { h6 }
            trailingPadding = 16.dp
        }
        ParagraphType.Text -> {
            textStyle = +themeTextStyle { body1 }
            paragraphStyle = paragraphStyle.copy(lineHeight = defaultBodyLineHeight)
        }
        ParagraphType.Header -> {
            textStyle = +themeTextStyle { h5 }
            trailingPadding = 16.dp
        }
        ParagraphType.CodeBlock -> textStyle = codeTextStyle
        ParagraphType.Quote -> textStyle = +themeTextStyle { body1 }
        ParagraphType.Bullet -> paragraphStyle = bulletParagraphStyle
    }
    return ParagraphStyling(textStyle, paragraphStyle, trailingPadding)
}

private fun paragraphToAnnotatedString(paragraph: Paragraph): AnnotatedString {
    val styles = paragraph.markups.map { it.toAnnotatedStringItem() }
    return AnnotatedString(text = paragraph.text, textStyles = styles)
}

private fun Markup.toAnnotatedStringItem(): AnnotatedString.Item<TextStyle> {
    return when (this.type) {
        MarkupType.Italic -> AnnotatedString.Item(italicTextStyle, start, end)
        MarkupType.Link -> AnnotatedString.Item(linkTextStyle, start, end)
        MarkupType.Bold -> AnnotatedString.Item(boldTextStyle, start, end)
        MarkupType.Code -> AnnotatedString.Item(codeTextStyle, start, end)
    }
}
