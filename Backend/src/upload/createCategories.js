const fs = require('fs')
const path = require('path')

// Load environment variables
require('dotenv').config({ path: path.join(__dirname, '../../.env') })

const { db } = require('../config/db')

/**
 * Tạo categories từ file categories.json và đẩy lên Firebase Realtime Database
 */
async function createCategories() {
  try {
    console.log('📖 Reading categories data from categories.json...')
    const categories = JSON.parse(fs.readFileSync(path.join(__dirname, 'categories.json'), 'utf8'))

    console.log(`📊 Found ${categories.length} categories:`)

    categories.forEach(cat => {
      console.log(`  - ID ${cat._id}: ${cat.name}`)
    })

    // Upload categories to Firebase theo cấu trúc categoryModel
    console.log('\n📤 Uploading categories to Firebase Realtime Database...')

    for (const category of categories) {
      try {
        // Tạo dữ liệu category theo cấu trúc categoryModel
        const categoryData = {
          _id: category._id,
          name: category.name.trim(),
          image_url: category.image_url.trim(),
          status: 'active',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }

        await db.ref(`categories/${category._id}`).set(categoryData)
        console.log(`✅ Created category ${category._id}: ${category.name}`)
      } catch (error) {
        console.error(`❌ Failed to create category ${category._id}: ${error.message}`)
      }
    }

    // Verify upload
    console.log('\n🔍 Verifying categories upload...')
    const snapshot = await db.ref('categories').once('value')
    const uploadedCategories = snapshot.val()
    const uploadedCount = uploadedCategories ? Object.keys(uploadedCategories).length : 0

    console.log(`📊 Total categories in Firebase: ${uploadedCount}`)

    if (uploadedCount > 0) {
      console.log('\n📋 Sample of uploaded categories:')
      const sampleCategories = Object.values(uploadedCategories).slice(0, 5)
      sampleCategories.forEach(cat => {
        console.log(`  - ID ${cat._id}: ${cat.name}`)
        console.log(`    Status: ${cat.status}, Created: ${cat.createdAt}`)
        console.log(`    Image: ${cat.image_url}`)
      })
    }

    return {
      totalCategories: categories.length,
      uploadedCount
    }

  } catch (error) {
    console.error('❌ Error creating categories:', error.message)
    throw error
  }
}

// Main execution
async function main() {
  try {
    await createCategories()
    console.log('\n🎉 Categories creation completed!')
  } catch (error) {
    console.error('💥 Script failed:', error)
    process.exit(1)
  }
}

// Run if called directly
if (require.main === module) {
  main()
}

module.exports = { createCategories }
