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

import nyab.match.QM

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=6] = QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
// https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/licensing-a-repository
// TODO add url
@Suppress("EnumEntryName")
enum class QMyLicense(val ghKey: String) {
    // CallChain[size=7] = QMyLicense.Apache_2_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    Apache_2_0("apache-2.0"),
    // CallChain[size=7] = QMyLicense.MIT <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    MIT("mit"),
    // CallChain[size=7] = QMyLicense.EPL_2_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    EPL_2_0("epl-2.0"),
    // CallChain[size=7] = QMyLicense.EPL_1_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    EPL_1_0("epl-1.0"),

    // CallChain[size=7] = QMyLicense.GPL_v3_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    GPL_v3_0("gpl-3.0"),
    // CallChain[size=7] = QMyLicense.GPL_v2_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    GPL_v2_0("gpl-2.0"),
    // CallChain[size=7] = QMyLicense.GPL_Family <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    GPL_Family("gpl"),

    // CallChain[size=7] = QMyLicense.LGPL_v3_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    LGPL_v3_0("lgpl-3.0"),
    // CallChain[size=7] = QMyLicense.LGPL_v2_1 <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    LGPL_v2_1("lgpl-2.1"),
    // CallChain[size=7] = QMyLicense.LGPL_Family <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    LGPL_Family("lgpl"),

    // CallChain[size=7] = QMyLicense.BSD_3_Clause_New_or_Revised_license <-[Propag]- QMyLicense <-[Ref] ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    BSD_3_Clause_New_or_Revised_license("bsd-3-clause"),
    // CallChain[size=7] = QMyLicense.BSD_3_Clause_Clear_license <-[Propag]- QMyLicense <-[Ref]- QDep.QD ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    BSD_3_Clause_Clear_license("bsd-3-clause-clear"),
    // CallChain[size=7] = QMyLicense.BSD_2_Clause <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    BSD_2_Clause("bsd-2-clause"),

    // CallChain[size=7] = QMyLicense.Academic_Free_License_v3_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QD ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    Academic_Free_License_v3_0("afl-3.0"),
    // CallChain[size=7] = QMyLicense.Artistic_license_2_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    Artistic_license_2_0("artistic-2.0"),
    // CallChain[size=7] = QMyLicense.Boost_Software_License_1_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QD ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    Boost_Software_License_1_0("bsl-1.0"),
    // CallChain[size=7] = QMyLicense.Creative_Commons_License_family <-[Propag]- QMyLicense <-[Ref]- QD ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    Creative_Commons_License_family("cc"),
    // CallChain[size=7] = QMyLicense.CC0_PublicDomain <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    CC0_PublicDomain("cc0-1.0"),
    // CallChain[size=7] = QMyLicense.CC_BY_4_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    CC_BY_4_0("cc-by-4.0"),
    // CallChain[size=7] = QMyLicense.CC_BY_SA_4_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    CC_BY_SA_4_0("cc-by-sa-4.0"),
    // CallChain[size=7] = QMyLicense.Do_What_The_Fuck_You_Want_To_Public_License <-[Propag]- QMyLicense ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    Do_What_The_Fuck_You_Want_To_Public_License("wtfpl"),
    // CallChain[size=7] = QMyLicense.Educational_Community_License_v2_0 <-[Propag]- QMyLicense <-[Ref]- ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    Educational_Community_License_v2_0("ecl-2.0"),
    // CallChain[size=7] = QMyLicense.European_Union_Public_License_1_1 <-[Propag]- QMyLicense <-[Ref]-  ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    European_Union_Public_License_1_1("eupl-1.1"),
    // CallChain[size=7] = QMyLicense.GNU_Affero_General_Public_License_v3_0 <-[Propag]- QMyLicense <-[R ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    GNU_Affero_General_Public_License_v3_0("agpl-3.0"),
    // CallChain[size=7] = QMyLicense.ISC <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    ISC("isc"),
    // CallChain[size=7] = QMyLicense.LaTeX_Project_Public_License_v1_3c <-[Propag]- QMyLicense <-[Ref]- ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    LaTeX_Project_Public_License_v1_3c("lppl-1.3c"),
    // CallChain[size=7] = QMyLicense.Microsoft_Public_License <-[Propag]- QMyLicense <-[Ref]- QDep.QDep ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    Microsoft_Public_License("ms-pl"),
    // CallChain[size=7] = QMyLicense.Mozilla_Public_License_1_1 <-[Propag]- QMyLicense <-[Ref]- QDep.QD ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    Mozilla_Public_License_1_1(""),
    // CallChain[size=7] = QMyLicense.Mozilla_Public_License_2_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QD ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    Mozilla_Public_License_2_0("mpl-2.0"),
    // CallChain[size=7] = QMyLicense.Open_Software_License_3_0 <-[Propag]- QMyLicense <-[Ref]- QDep.QDe ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    Open_Software_License_3_0("osl-3.0"),
    // CallChain[size=7] = QMyLicense.PostgreSQL_License <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    PostgreSQL_License("postgresql"),
    // CallChain[size=7] = QMyLicense.SIL_Open_Font_License_1_1 <-[Propag]- QMyLicense <-[Ref]- QDep.QDe ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    SIL_Open_Font_License_1_1("ofl-1.1"),
    // CallChain[size=7] = QMyLicense.University_of_Illinois_NCSA_Open_Source_License <-[Propag]- QMyLic ... Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    University_of_Illinois_NCSA_Open_Source_License("ncsa"),
    // CallChain[size=7] = QMyLicense.The_Unlicense <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    The_Unlicense("unlicense"),
    // CallChain[size=7] = QMyLicense.zLib_License <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    zLib_License("zlib"),
    // CallChain[size=7] = QMyLicense.OracleOrSunMicro <-[Propag]- QMyLicense <-[Ref]- QDep.QDep() <-[Call]- QMyDepI.UNKNOWN_LICENSE_LIBS <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    OracleOrSunMicro("");

    companion object {
        
    }
}