/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.toolbar.sub

import android.content.Context
import android.graphics.Color
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView

import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.theme.ThemeData
import jp.hazuki.yuzubrowser.utils.ImeUtils
import jp.hazuki.yuzubrowser.webkit.CustomWebView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.find_onpage.*

object WebViewFindDialogFactory {
    fun createInstance(context: Context, layout: View): WebViewFindDialog {
        return WebViewFind(context, layout)
    }

    private class WebViewFind(private val mContext: Context, override val containerView: View) : WebViewFindDialog, LayoutContainer {
        private var mCurrentWeb: CustomWebView? = null

        override val isVisible: Boolean
            get() = containerView.visibility == View.VISIBLE

        private val findListener = WebView.FindListener { activeMatchOrdinal, numberOfMatches, _ ->
            howMatchTextView.text = (if (numberOfMatches > 0) activeMatchOrdinal + 1 else 0).toString() + "/" + numberOfMatches
        }

        override fun show(web: CustomWebView) {
            mCurrentWeb = web
            web.setFindListener(findListener)

            if (ThemeData.isEnabled()) {
                val data = ThemeData.getInstance()

                if (data.toolbarBackgroundColor != 0)
                    containerView.setBackgroundColor(data.toolbarBackgroundColor)
                else
                    containerView.setBackgroundResource(R.color.deep_gray)

                if (data.toolbarTextColor != 0) {
                    findEditText.run {
                        setTextColor(data.toolbarTextColor)
                        setHintTextColor(data.toolbarTextColor and 0xffffff or -0x78000000)
                    }
                    howMatchTextView.setTextColor(data.toolbarTextColor)
                } else {
                    findEditText.run {
                        setTextColor(Color.WHITE)
                        setHintTextColor(-0x77000001)
                    }
                    howMatchTextView.setTextColor(Color.WHITE)
                }

                if (data.toolbarImageColor != 0) {
                    buttonLeft.setColorFilter(data.toolbarImageColor)
                    buttonRight.setColorFilter(data.toolbarImageColor)
                    buttonEnd.setColorFilter(data.toolbarImageColor)
                } else {
                    buttonLeft.clearColorFilter()
                    buttonRight.clearColorFilter()
                    buttonEnd.clearColorFilter()
                }
            }

            containerView.visibility = View.VISIBLE
            findEditText.run {
                requestFocus()
                postDelayed({
                    dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0f, 0f, 0))
                    dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0f, 0f, 0))
                }, 100)
                setText("")

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        mCurrentWeb?.run {
                            clearMatches()
                            findAllAsync(s.toString())
                        }
                    }

                    override fun afterTextChanged(s: Editable) {}
                })
            }

            buttonLeft.setOnClickListener {
                mCurrentWeb?.run {
                    ImeUtils.hideIme(mContext, findEditText)

                    findNext(false)
                    requestFocus()
                }
            }

            buttonRight.setOnClickListener {
                mCurrentWeb?.run {
                    ImeUtils.hideIme(mContext, findEditText)

                    findNext(true)
                    requestFocus()
                }
            }

            buttonEnd.setOnClickListener { hide() }
        }

        override fun hide() {
            containerView.visibility = View.GONE
            ImeUtils.hideIme(mContext, findEditText)

            mCurrentWeb?.run {
                clearMatches()
                notifyFindDialogDismissedMethod()
            }
        }
    }
}
