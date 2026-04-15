<template>
  <div class="admin-page">
    <h1>Admin Panel</h1>
    <div class="admin-content">
      <div class="users-list">
        <h2>All Users</h2>
        <div v-if="loading" class="loading">Loading...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else class="users-grid">
          <div v-for="user in users" :key="user.userId" class="user-card">
            <div class="user-info">
              <h3>{{ user.fullName }}</h3>
              <p>{{ user.email }}</p>
              <p>Role: {{ user.role }}</p>
              <p>Allergens: {{ user.myAllergens || 'None' }}</p>
            </div>
            <div class="user-actions">
              <button @click="editUser(user)" class="btn btn-secondary">Edit</button>
              <button @click="deleteUser(user.userId)" class="btn btn-danger">Delete</button>
              <select @change="changeRole(user.userId, $event.target.value)" :value="user.role">
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <div v-if="editingUser" class="modal-overlay" @click="closeEdit">
      <div class="modal" @click.stop>
        <h2>Edit User</h2>
        <form @submit.prevent="saveUser">
          <div class="form-group">
            <label>Full Name:</label>
            <input v-model="editForm.fullName" type="text" required />
          </div>
          <div class="form-group">
            <label>Allergens:</label>
            <textarea v-model="editForm.myAllergens"></textarea>
          </div>
          <div class="form-actions">
            <button type="button" @click="closeEdit" class="btn btn-secondary">Cancel</button>
            <button type="submit" class="btn btn-primary">Save</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import * as authApi from '../api/auth'

const auth = useAuthStore()

const users = ref([])
const loading = ref(false)
const error = ref('')
const editingUser = ref(null)
const editForm = ref({ fullName: '', myAllergens: '' })

const fetchUsers = async () => {
  loading.value = true
  error.value = ''
  try {
    users.value = await authApi.getAllUsers()
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load users'
  } finally {
    loading.value = false
  }
}

const deleteUser = async (userId) => {
  if (!confirm('Are you sure you want to delete this user?')) return
  try {
    await authApi.deleteUser(userId)
    await fetchUsers()
  } catch (err) {
    alert('Failed to delete user')
  }
}

const changeRole = async (userId, role) => {
  try {
    await authApi.updateUserRole(userId, role)
    await fetchUsers()
  } catch (err) {
    alert('Failed to update role')
  }
}

const editUser = (user) => {
  editingUser.value = user
  editForm.value = {
    fullName: user.fullName,
    myAllergens: user.myAllergens || ''
  }
}

const closeEdit = () => {
  editingUser.value = null
}

const saveUser = async () => {
  try {
    await authApi.updateUserName(editingUser.value.userId, editForm.value.fullName)
    await authApi.updateUserAllergens(editingUser.value.userId, editForm.value.myAllergens)
    await fetchUsers()
    closeEdit()
  } catch (err) {
    alert('Failed to update user')
  }
}

onMounted(() => {
  if (!auth.isAdmin) {
    // Redirect if not admin
    return
  }
  fetchUsers()
})
</script>

<style scoped>
.admin-page {
  padding: 20px;
}

.admin-content {
  margin-top: 20px;
}

.users-list h2 {
  margin-bottom: 20px;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.user-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  background: white;
}

.user-info h3 {
  margin: 0 0 10px 0;
}

.user-info p {
  margin: 5px 0;
}

.user-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.btn {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>
