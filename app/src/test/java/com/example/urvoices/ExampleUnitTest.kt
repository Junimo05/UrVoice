import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.service.FirebaseAudioService
import com.example.urvoices.data.service.FirebasePostService
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

//class FirebasePostServiceTest {
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun testGetComments_Posts(): Unit = runTest {
//        // Tạo mock cho FirebaseFirestore và Query
//        val mockFirestore = mock(FirebaseFirestore::class.java)
//        val mockCollectionReference = mock(CollectionReference::class.java)
//        val mockQuery = mock(Query::class.java)
//
//        // Tạo mock cho QuerySnapshot và DocumentSnapshot
//        val mockQuerySnapshot = mock(QuerySnapshot::class.java)
//        val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)
//
//        // Thiết lập giả lập cho các phương thức cần thiết
//        `when`(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference)
//        `when`(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery)
//        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
//        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
//        `when`(mockDocumentSnapshot.getString(anyString())).thenReturn("comment1")
//        `when`(mockDocumentSnapshot.getLong(anyString())).thenReturn(123456789L)
//
//        // Tạo một instance của FirebasePostService với FirebaseFirestore đã được giả lập
//        val firebasePostService = FirebasePostService(
//            audioManager = mock(AudioManager::class.java),
//            mockFirestore,
//            mock(StorageReference::class.java),
//            mockk<FirebaseAudioService>(),
//
//        )
//
//        // Gọi hàm cần kiểm tra
//        val result = firebasePostService.getComments_Posts("post1")
//
//        // Kiểm tra xem kết quả có đúng như mong đợi hay không
//        // (trong trường hợp này, chúng ta mong đợi một danh sách chứa một Comments với các trường đều là "test" và 123456789L cho các trường Long)
//        assertEquals(1, result.size)
//        assertEquals("comment1", result[0].id)
//        assertEquals("user1", result[0].userId)
//
//        // ... và cứ như vậy cho tất cả các trường khác
//    }
//}