/*
 * Copyright (C) 2022 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.libcore.regression;

import android.perftests.utils.BenchmarkState;
import android.perftests.utils.PerfStatusReporter;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

/**
 * Tests various Character methods, intended for testing multiple implementations against each
 * other.
 */
@RunWith(Parameterized.class)
@LargeTest
public class CharacterPerfTest {
    @Rule public PerfStatusReporter mPerfStatusReporter = new PerfStatusReporter();

    @Parameters(name = "mCharacterSet({0}), mOverload({1})")
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {CharacterSet.ASCII, Overload.CHAR},
                    {CharacterSet.ASCII, Overload.INT},
                    {CharacterSet.UNICODE, Overload.CHAR},
                    {CharacterSet.UNICODE, Overload.INT}
                });
    }

    @Parameterized.Parameter(0)
    public CharacterSet mCharacterSet;

    @Parameterized.Parameter(1)
    public Overload mOverload;

    private char[] mChars;

    @Before
    public void setUp() throws Exception {
        this.mChars = mCharacterSet.mChars;
    }

    public enum Overload {
        CHAR,
        INT
    }

    public double nanosToUnits(double nanos) {
        return nanos / 65536;
    }

    public enum CharacterSet {
        ASCII(128),
        UNICODE(65536);
        final char[] mChars;

        CharacterSet(int size) {
            this.mChars = new char[65536];
            for (int i = 0; i < 65536; ++i) {
                mChars[i] = (char) (i % size);
            }
        }
    }

    // A fake benchmark to give us a baseline.
    @Test
    public void timeIsSpace() {
        boolean fake = false;
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    fake ^= ((char) ch == ' ');
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    fake ^= (ch == ' ');
                }
            }
        }
    }

    @Test
    public void timeDigit() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.digit(mChars[ch], 10);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.digit((int) mChars[ch], 10);
                }
            }
        }
    }

    @Test
    public void timeGetNumericValue() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.getNumericValue(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.getNumericValue((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeIsDigit() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isDigit(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isDigit((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeIsIdentifierIgnorable() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isIdentifierIgnorable(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isIdentifierIgnorable((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeIsJavaIdentifierPart() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isJavaIdentifierPart(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isJavaIdentifierPart((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeIsJavaIdentifierStart() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isJavaIdentifierStart(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isJavaIdentifierStart((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeIsLetter() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isLetter(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isLetter((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeIsLetterOrDigit() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isLetterOrDigit(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isLetterOrDigit((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeIsLowerCase() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isLowerCase(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isLowerCase((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeIsSpaceChar() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isSpaceChar(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isSpaceChar((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeIsUpperCase() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isUpperCase(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isUpperCase((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeIsWhitespace() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isWhitespace(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.isWhitespace((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeToLowerCase() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.toLowerCase(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.toLowerCase((int) mChars[ch]);
                }
            }
        }
    }

    @Test
    public void timeToUpperCase() {
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();
        if (mOverload == Overload.CHAR) {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.toUpperCase(mChars[ch]);
                }
            }
        } else {
            while (state.keepRunning()) {
                for (int ch = 0; ch < 65536; ++ch) {
                    Character.toUpperCase((int) mChars[ch]);
                }
            }
        }
    }
}
