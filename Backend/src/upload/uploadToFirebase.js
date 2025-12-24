const fs = require('fs')
const path = require('path')

// Load environment variables
require('dotenv').config({ path: path.join(__dirname, '../../.env') })

const { db } = require('../config/db')

/**
 * Upload books to Firebase
 * @param {string} filePath - Path to JSON file
 * @param {number} batchSize - Number of books to upload per batch
 * @param {boolean} isTest - Whether this is a test upload
 */
async function uploadBooksToFirebase(filePath, batchSize = 50, isTest = false) {
  try {
    console.log(`📖 Reading books from: ${filePath}`)
    const books = JSON.parse(fs.readFileSync(filePath, 'utf8'))

    console.log(`📊 Total books to upload: ${books.length}`)
    console.log(`🔄 Batch size: ${batchSize}`)

    if (isTest) {
      console.log('🧪 TEST MODE: Only uploading first 10 books')
    }

    const booksToUpload = isTest ? books.slice(0, 10) : books
    const totalBatches = Math.ceil(booksToUpload.length / batchSize)

    console.log(`📦 Processing ${totalBatches} batches...`)

    for (let i = 0; i < totalBatches; i++) {
      const startIndex = i * batchSize
      const endIndex = Math.min(startIndex + batchSize, booksToUpload.length)
      const batch = booksToUpload.slice(startIndex, endIndex)

      console.log(`\n📤 Uploading batch ${i + 1}/${totalBatches} (books ${startIndex + 1}-${endIndex})`)

      // Upload batch to Firebase
      const batchPromises = batch.map(async (book) => {
        try {
          await db.ref(`books/${book._id}`).set(book)
          return { success: true, id: book._id, title: book.title }
        } catch (error) {
          console.error(`❌ Failed to upload book ${book._id}: ${error.message}`)
          return { success: false, id: book._id, title: book.title, error: error.message }
        }
      })

      const results = await Promise.all(batchPromises)

      const successCount = results.filter(r => r.success).length
      const failCount = results.filter(r => !r.success).length

      console.log(`✅ Success: ${successCount}, ❌ Failed: ${failCount}`)

      // Show failed books
      if (failCount > 0) {
        const failedBooks = results.filter(r => !r.success)
        console.log('Failed books:')
        failedBooks.forEach(book => {
          console.log(`  - ${book.id}: ${book.title} (${book.error})`)
        })
      }

      // Add delay between batches to avoid rate limiting
      if (i < totalBatches - 1) {
        console.log('⏳ Waiting 1 second before next batch...')
        await new Promise(resolve => setTimeout(resolve, 1000))
      }
    }

    console.log('\n🎉 Upload completed!')

    // Verify upload
    console.log('\n🔍 Verifying upload...')
    const snapshot = await db.ref('books').once('value')
    const uploadedBooks = snapshot.val()
    const uploadedCount = uploadedBooks ? Object.keys(uploadedBooks).length : 0

    console.log(`📊 Total books in Firebase: ${uploadedCount}`)

    if (uploadedCount > 0) {
      console.log('\n📋 Sample of uploaded books:')
      const sampleBooks = Object.values(uploadedBooks).slice(0, 5)
      sampleBooks.forEach(book => {
        console.log(`  - ${book._id}: ${book.title} by ${book.author}`)
      })
    }

  } catch (error) {
    console.error('❌ Upload failed:', error.message)
    throw error
  }
}

/**
 * Clear all books from Firebase (use with caution!)
 */
async function clearAllBooks() {
  try {
    console.log('🗑️  Clearing all books from Firebase...')
    await db.ref('books').remove()
    console.log('✅ All books cleared!')
  } catch (error) {
    console.error('❌ Failed to clear books:', error.message)
    throw error
  }
}

// Main execution
async function main() {
  const args = process.argv.slice(2)
  const command = args[0]

  switch (command) {
    case 'test':
      console.log('🧪 Starting TEST upload...')
      await uploadBooksToFirebase(
        path.join(__dirname, 'book_test.json'),
        10,
        true
      )
      break

    case 'full':
      console.log('📚 Starting FULL upload...')
      await uploadBooksToFirebase(
        path.join(__dirname, 'book_firebase.json'),
        50,
        false
      )
      break

    case 'clear':
      console.log('🗑️  Clearing all books...')
      await clearAllBooks()
      break

    default:
      console.log(`
📖 Firebase Book Uploader

Usage:
  node uploadToFirebase.js test   - Upload test data (10 books)
  node uploadToFirebase.js full   - Upload all books (742 books)
  node uploadToFirebase.js clear  - Clear all books from Firebase

Examples:
  node uploadToFirebase.js test
  node uploadToFirebase.js full
      `)
  }
}

// Run if called directly
if (require.main === module) {
  main().catch(error => {
    console.error('💥 Script failed:', error)
    process.exit(1)
  })
}

module.exports = {
  uploadBooksToFirebase,
  clearAllBooks
}
