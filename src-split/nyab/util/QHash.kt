/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.util

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=10] = QHash <-[Ref]- Path.qHash() <-[Call]- Path.qCopyFileTo() <-[Call]- QBackupFi ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal enum class QHash(val str: String) {
    // region

    // CallChain[size=12] = QHash.MD2 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Call]- Pat ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    MD2("MD2"),
    // CallChain[size=12] = QHash.MD5 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Call]- Pat ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    MD5("MD5"),
    // CallChain[size=12] = QHash.SHA_1 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Call]- P ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    SHA_1("SHA-1"),
    // CallChain[size=12] = QHash.SHA_224 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Call]- ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    SHA_224("SHA-224"),
    // CallChain[size=12] = QHash.SHA_256 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Call]- ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    SHA_256("SHA-256"),
    // CallChain[size=12] = QHash.SHA_384 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Call]- ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    SHA_384("SHA-384"),
    // CallChain[size=12] = QHash.SHA_512 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Call]- ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    SHA_512("SHA-512"),
    // CallChain[size=12] = QHash.SHA_512_224 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Ca ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    SHA_512_224("SHA-512/224"),
    // CallChain[size=12] = QHash.SHA_512_256 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Ca ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    SHA_512_256("SHA-512/256"),
    // CallChain[size=12] = QHash.SHA3_224 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Call] ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    SHA3_224("SHA3-224"),
    // CallChain[size=12] = QHash.SHA3_256 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Call] ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    SHA3_256("SHA3-256"),
    // CallChain[size=12] = QHash.SHA3_384 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Call] ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    SHA3_384("SHA3-384"),
    // CallChain[size=12] = QHash.SHA3_512 <-[Propag]- QHash.QHash() <-[Call]- Path.qHashFile() <-[Call] ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    SHA3_512("SHA3-512");

    // endregion

    companion object {
        // CallChain[size=10] = QHash.DEFAULT <-[Call]- Path.qHash() <-[Call]- Path.qCopyFileTo() <-[Call]-  ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
        val DEFAULT = SHA3_512
    }
}