package com.zachklipp.richtext.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zachklipp.richtext.adf.AdfRenderer

@Preview
@Composable private fun AdfSamplePreview() {
  AdfSample()
}

@Composable fun AdfSample() {
  var isDarkModeEnabled by remember { mutableStateOf(false) }

  val colors = if (isDarkModeEnabled) darkColors() else lightColors()

  MaterialTheme(colors = colors) {
    Surface {
      Column(Modifier.verticalScroll(rememberScrollState())) {
        AdfRenderer(
          content = adfContent,
          modifier = Modifier
            .padding(16.dp)
        )
      }
    }
  }
}

private val adfContent = """
  {
  "version": 1,
  "type": "doc",
  "content": [
    {
      "type": "heading",
      "attrs": {
        "level": 1
      },
      "content": [
        {
          "type": "text",
          "text": "Header 1"
        }
      ]
    },
    {
      "type": "heading",
      "attrs": {
        "level": 2
      },
      "content": [
        {
          "type": "text",
          "text": "Header 2"
        }
      ]
    },
    {
      "type": "heading",
      "attrs": {
        "level": 3
      },
      "content": [
        {
          "type": "text",
          "text": "Header 3"
        }
      ]
    },
    {
      "type": "heading",
      "attrs": {
        "level": 4
      },
      "content": [
        {
          "type": "text",
          "text": "Header 4"
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "bold",
          "marks": [
            {
              "type": "strong"
            }
          ]
        },
        {
          "type": "text",
          "text": " "
        },
        {
          "type": "text",
          "text": "linked",
          "marks": [
            {
              "type": "link",
              "attrs": {
                "href": "https://google.com"
              }
            }
          ]
        },
        {
          "type": "text",
          "text": " "
        },
        {
          "type": "text",
          "text": "italic ",
          "marks": [
            {
              "type": "em"
            }
          ]
        },
        {
          "type": "text",
          "text": "code",
          "marks": [
            {
              "type": "code"
            }
          ]
        },
        {
          "type": "text",
          "text": " and "
        },
        {
          "type": "text",
          "text": "subscript ",
          "marks": [
            {
              "type": "strike"
            },
            {
              "type": "subsup",
              "attrs": {
                "type": "sub"
              }
            }
          ]
        },
        {
          "type": "text",
          "text": "superscript",
          "marks": [
            {
              "type": "strike"
            },
            {
              "type": "subsup",
              "attrs": {
                "type": "sup"
              }
            }
          ]
        },
        {
          "type": "text",
          "text": " ",
          "marks": [
            {
              "type": "subsup",
              "attrs": {
                "type": "sup"
              }
            }
          ]
        },
        {
          "type": "text",
          "text": "ends"
        }
      ]
    },
    {
      "type": "table",
      "attrs": {
        "isNumberColumnEnabled": false,
        "layout": "default"
      },
      "content": [
        {
          "type": "tableRow",
          "content": [
            {
              "type": "tableHeader",
              "attrs": {},
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "first header",
                      "marks": [
                        {
                          "type": "strong"
                        }
                      ]
                    }
                  ]
                }
              ]
            },
            {
              "type": "tableHeader",
              "attrs": {},
              "content": [
                {
                  "type": "paragraph",
                  "content": []
                }
              ]
            },
            {
              "type": "tableHeader",
              "attrs": {},
              "content": [
                {
                  "type": "paragraph",
                  "content": []
                }
              ]
            }
          ]
        },
        {
          "type": "tableRow",
          "content": [
            {
              "type": "tableCell",
              "attrs": {},
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "second content"
                    }
                  ]
                }
              ]
            },
            {
              "type": "tableCell",
              "attrs": {},
              "content": [
                {
                  "type": "bulletList",
                  "content": [
                    {
                      "type": "listItem",
                      "content": [
                        {
                          "type": "paragraph",
                          "content": [
                            {
                              "type": "text",
                              "text": "let’s add"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "type": "listItem",
                      "content": [
                        {
                          "type": "paragraph",
                          "content": [
                            {
                              "type": "text",
                              "text": "bullet points in tables"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            },
            {
              "type": "tableCell",
              "attrs": {},
              "content": [
                {
                  "type": "blockquote",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {
                          "type": "text",
                          "text": "Quotes"
                        }
                      ]
                    }
                  ]
                },
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "and"
                    }
                  ]
                },
                {
                  "type": "codeBlock",
                  "attrs": {},
                  "content": [
                    {
                      "type": "text",
                      "text": "code blocks\nmultiple lines"
                    }
                  ]
                }
              ]
            }
          ]
        },
        {
          "type": "tableRow",
          "content": [
            {
              "type": "tableCell",
              "attrs": {},
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "another also content"
                    }
                  ]
                }
              ]
            },
            {
              "type": "tableCell",
              "attrs": {},
              "content": [
                {
                  "type": "panel",
                  "attrs": {
                    "panelType": "info"
                  },
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {
                          "type": "text",
                          "text": "Info panel also exists"
                        }
                      ]
                    }
                  ]
                }
              ]
            },
            {
              "type": "tableCell",
              "attrs": {},
              "content": [
                {
                  "type": "paragraph",
                  "content": []
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "blockquote",
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "Try not to become a man of success, but rather try to become a man of value."
            }
          ]
        }
      ]
    },
    {
      "type": "panel",
      "attrs": {
        "panelType": "info"
      },
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "This is an ADG info Panel "
            }
          ]
        }
      ]
    },
    {
      "type": "panel",
      "attrs": {
        "panelType": "warning"
      },
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "There is a warning"
            }
          ]
        }
      ]
    },
    {
      "type": "panel",
      "attrs": {
        "panelType": "error"
      },
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "Failed"
            }
          ]
        },
        {
          "type": "bulletList",
          "content": [
            {
              "type": "listItem",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "Some"
                    }
                  ]
                }
              ]
            },
            {
              "type": "listItem",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "Reasons"
                    }
                  ]
                }
              ]
            },
            {
              "type": "listItem",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "Listed"
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "rule"
    },
    {
      "type": "codeBlock",
      "attrs": {
        "language": "kotlin"
      },
      "content": [
        {
          "type": "text",
          "text": "val adfParser = AdfParser()\nval adfTreeNode = adfParser.parse(adfContent)\nif (adfTreeNode != oldAdfTreeNode && adfTreeNode != newAdfTreeNode) {\n  indent.test()\n}"
        }
      ]
    },
    {
      "type": "bulletList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "bullet points"
                }
              ]
            }
          ]
        },
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "and"
                }
              ]
            },
            {
              "type": "bulletList",
              "content": [
                {
                  "type": "listItem",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {
                          "type": "text",
                          "text": "even"
                        }
                      ]
                    },
                    {
                      "type": "bulletList",
                      "content": [
                        {
                          "type": "listItem",
                          "content": [
                            {
                              "type": "paragraph",
                              "content": [
                                {
                                  "type": "text",
                                  "text": "cascading"
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  ]
                },
                {
                  "type": "listItem",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {
                          "type": "text",
                          "text": "indentation"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "orderedList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "Also"
                }
              ]
            }
          ]
        },
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "Ordered"
                }
              ]
            }
          ]
        },
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "List"
                }
              ]
            },
            {
              "type": "orderedList",
              "content": [
                {
                  "type": "listItem",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {
                          "type": "text",
                          "text": "that"
                        }
                      ]
                    },
                    {
                      "type": "orderedList",
                      "content": [
                        {
                          "type": "listItem",
                          "content": [
                            {
                              "type": "paragraph",
                              "content": [
                                {
                                  "type": "text",
                                  "text": "also"
                                }
                              ]
                            },
                            {
                              "type": "orderedList",
                              "content": [
                                {
                                  "type": "listItem",
                                  "content": [
                                    {
                                      "type": "paragraph",
                                      "content": [
                                        {
                                          "type": "text",
                                          "text": "cascades"
                                        }
                                      ]
                                    }
                                  ]
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "paragraph",
      "content": []
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "You can add mentions"
        },
        {
          "type": "text",
          "text": " ",
          "marks": [
            {
              "type": "strong"
            }
          ]
        },
        {
          "type": "mention",
          "attrs": {
            "id": "0",
            "text": "@Carolyn",
            "accessLevel": ""
          }
        },
        {
          "type": "text",
          "text": " ",
          "marks": [
            {
              "type": "strong"
            }
          ]
        },
        {
          "type": "mention",
          "attrs": {
            "id": "1",
            "text": "@Kaitlyn Prouty",
            "accessLevel": ""
          }
        },
        {
          "type": "text",
          "text": " ",
          "marks": [
            {
              "type": "strong"
            }
          ]
        },
        {
          "type": "mention",
          "attrs": {
            "id": "2",
            "text": "@Verdie Carrales",
            "accessLevel": ""
          }
        },
        {
          "type": "text",
          "text": " ",
          "marks": [
            {
              "type": "strong"
            }
          ]
        },
        {
          "type": "text",
          "text": "and more text after and this paragraph is longer without mentions"
        }
      ]
    },
    {
      "type": "paragraph",
      "content": []
    }
  ]
}
  """.trimIndent()