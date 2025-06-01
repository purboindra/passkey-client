package com.purboyndradev.saferauth.ui

import androidx.compose.ui.graphics.vector.ImageVector
import com.purboyndradev.saferauth.ui.myiconpack.IconPasskey
import kotlin.collections.List as ____KtList

public object MyIconPack

private var __AllIcons: ____KtList<ImageVector>? = null

public val MyIconPack.AllIcons: ____KtList<ImageVector>
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons= listOf(IconPasskey)
    return __AllIcons!!
  }
