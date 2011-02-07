/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_error_h
#define __inc_og_language_util_error_h

// Rrror reporting/handling

#include <errno.h>

#ifndef _WIN32
#define GetLastError() errno
#define SetLastError(_expr_) (errno = (_expr_))
#endif /* ifndef _WIN32 */

inline bool PosixLastError(int result) {
	if (result == 0) return true;
	SetLastError (result);
	return false;
}

#ifdef __cplusplus_cli
#pragma unmanaged
static int NativeGetLastError () {
	return GetLastError ();
}
#pragma managed
#else /* ifdef __cplusplus_cli */
#define NativeGetLastError GetLastError
#endif /* ifdef __cplusplus_cli */

#ifdef _WIN32
// Don't use POSIX compatible calls, so replace the POSIX names with Win32 error codes
#undef ECANCELED
#define ECANCELED		ERROR_CANCELLED
#undef ENOENT
#define ENOENT			ERROR_FILE_NOT_FOUND
#undef ETIMEDOUT
#define ETIMEDOUT		ERROR_TIMEOUT
#endif /* ifdef _WIN32 */

#endif /* ifndef __inc_og_language_util_error_h */