/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.conf

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=3] = QMyEditor <-[Ref]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
internal enum class QMyEditor(val cmd: String) {
    // CallChain[size=3] = QMyEditor.PlatformDefault <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
    PlatformDefault(""),
    // CallChain[size=4] = QMyEditor.VSCode <-[Propag]- QMyEditor.QMyEditor() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
    VSCode("code"),
    // CallChain[size=3] = QMyEditor.Idea <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
    Idea("idea64");
}