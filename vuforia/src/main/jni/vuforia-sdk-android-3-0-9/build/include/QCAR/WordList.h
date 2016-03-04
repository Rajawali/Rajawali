/*===============================================================================
Copyright (c) 2013-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    WordList.h

@brief
    Header file for WordList class.
===============================================================================*/
#ifndef _QCAR_WORD_LIST_H_
#define _QCAR_WORD_LIST_H_

#include <QCAR/System.h>
#include <QCAR/NonCopyable.h>
#include <QCAR/QCAR.h>

namespace QCAR
{

/// A list of words that the TextTracker can detect and track
/**
 *  The WordList represents the set of detectable Words. This list is
 *  loaded from a binary data file using loadWordList.
 *  By default a WordList for the English language is provided with the SDK.
 *  The application may choose to add a small set of additional custom words to
 *  the WordList using the APIs below.
 *  The filter list allows an application to specify a subset of Words
 *  from the WordList that will be detected and tracked. 
 *  Due to the large number of words in the WordList it is not possible to
 *  query the contents of the WordList at run-time.
 *  Note that the TextTracker needs to be stopped prior to making modifications
 *  to the WordList.
 */
class QCAR_API WordList : public NonCopyable
{
public:

    /// Deprecated enum.  Use QCAR::STORAGE_TYPE instead.
    /// Types of storage locations
    enum STORAGE_TYPE {
        STORAGE_APP,            ///< Storage private to the application
        STORAGE_APPRESOURCE,    ///< Storage for assets bundled with the
                                ///< application
        STORAGE_ABSOLUTE        ///< Helper type for specifying an absolute path
    };

    /// Types of filter modes
    enum FILTER_MODE {
        FILTER_MODE_NONE,       ///< Word filtering is disabled
        FILTER_MODE_BLACK_LIST, ///< Prevent specific words from being detected
        FILTER_MODE_WHITE_LIST  ///< Enable selected words only to be detected
    };

    /// Loads the word list from a binary file at the specified path 
    /// and storage location.
    /**
     *  Loads the word list from the given input file.
     *  Returns false if the path is NULL.
     */
    virtual bool loadWordList(const char* path, QCAR::STORAGE_TYPE storageType) = 0;

    /// Loads the word list from a binary file at the specified path 
    /// and storage location.
    /**
     *  Loads the word list from the given input file.
     *  Returns false if the path is NULL.
     *  
     *  This version is now deprecated, please use QCAR::STORAGE_TYPE based 
     *  method instead.
     */
    virtual bool loadWordList(const char* path, STORAGE_TYPE storageType) = 0;

    /// Loads a set of custom words from a plain text file
    /**
     *  The word list is extended with the custom words in the plain text file.
     *  Each word must be between 2-45 characters in length. Returns the
     *  number of loaded custom words. The text file shall be encoded in UTF-8.
     *  If path is NULL the return value is -1.
     */
    virtual int addWordsFromFile(const char* path, QCAR::STORAGE_TYPE storageType) = 0;

    /// Loads a set of custom words from a plain text file
    /**
     *  The word list is extended with the custom words in the plain text file.
     *  Each word must be between 2-45 characters in length. Returns the
     *  number of loaded custom words. The text file shall be encoded in UTF-8.
     *  If path is NULL the return value is -1.
     *  
     *  This version is now deprecated, please use QCAR::STORAGE_TYPE based 
     *  method instead.
     */
    virtual int addWordsFromFile(const char* path, STORAGE_TYPE storageType) = 0;

    /// Add a single custom word to the word list (Unicode)
    /**
     *  Use containsWord to check if the word is already in the word list prior
     *  calling this.
     *  Returns false if word is NULL;
     */
    virtual bool addWordU(const UInt16* word) = 0;

    /// Remove a custom word from the word list (Unicode)
    virtual bool removeWordU(const UInt16* word) = 0;

    /// Returns true if the given word is present in the WordList (Unicode)
    /**
     *  This function can be used to check if a word already exists in the
     *  WordList prior to adding it as a custom word.
     *  Returns false if word is NULL;
     */
    virtual bool containsWordU(const UInt16* word) = 0;

    /// Clears the word list as well as the filter list.
    /**
     *  Call this to reset the word list and release all acquired system
     *  resources.
     *  Return false if word is NULL;
     */
    virtual bool unloadAllLists() = 0;

    /// Sets the mode for the filter list
    /**
     *  The filter list allows an application to specify a subset of Words
     *  from the word list that will be detected and tracked. It can do this
     *  in two modes of operation. In black list mode, any word in the filter
     *  list will be prevented from being detected. In the white list mode,
     *  only words in the the filter list can be detected.
     *  By default the filter mode is FILTER_MODE_NONE where no words are
     *  filtered.
     */
    virtual bool setFilterMode(FILTER_MODE mode) = 0;

    /// Returns the filter mode.
    virtual FILTER_MODE getFilterMode() const = 0;

    /// Add a single word to the filter list (Unicode)
    /**
     *  Adds a word to the filter list.
     *  Returns true if successful, false if unsuccessful or if word
     *  is NULL.
     */
    virtual bool addWordToFilterListU(const UInt16* word) = 0;

    /// Remove a word from the filter list (Unicode)
    /**
     *  Remove a word from the filter list
     *  Returns true if successful, false if unsuccessful or if word
     *  is NULL.
     */
    virtual bool removeWordFromFilterListU(const UInt16* word) = 0;

    /// Clear the filter list.
    virtual bool clearFilterList() = 0;

    /// Loads the filter list from a plain text file.
    /**
     *  The text file shall be encoded in UTF-8.
     *  Returns false if the filter list cannot be loaded.  Note
     *  some words may have been added to the filter list so it
     *  may be necessary to call getFilterListWordCount to find
     *  out what, if any, words have been loaded by this routine
     *  if it fails.
     *  
     *  This version is now deprecated, please use QCAR::STORAGE_TYPE based 
     *  method instead.
     */
    virtual bool loadFilterList(const char* path, STORAGE_TYPE storageType) = 0;

    /// Loads the filter list from a plain text file.
    /**
     *  The text file shall be encoded in UTF-8.
     *  Returns false if the filter list cannot be loaded.  Note
     *  some words may have been added to the filter list so it
     *  may be necessary to call getFilterListWordCount to find
     *  out what, if any, words have been loaded by this routine
     *  if it fails.
     */
    virtual bool loadFilterList(const char* path, QCAR::STORAGE_TYPE storageType) = 0;

    /// Query the number of words in the filter list.
    virtual int getFilterListWordCount() = 0;
    
    /// Return the ith element in the filter list (Unicode)
    virtual const UInt16* getFilterListWordU(int i) = 0;

};
} // namespace QCAR

#endif /* _QCAR_WORD_LIST_H_ */
