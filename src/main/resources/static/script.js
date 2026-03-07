function searchAddress() {
    new kakao.Postcode({
        oncomplete: function(data) {
            // 주소 필드 세팅
            document.getElementById('postcode').value = data.zonecode;
            document.getElementById('roadAddress').value = data.roadAddress;

            // 감자님의 엔티티 구조를 위한 코드 추출
            document.getElementById('sidoCode').value = data.bcode.substring(0, 2); // bcode 앞 2자리
            document.getElementById('sidoName').value = data.sido;
            document.getElementById('sigunguCode').value = data.sigunguCode;
            document.getElementById('sigunguName').value = data.sigungu;
            document.getElementById('dongCode').value = data.bcode; // 법정동 10자리 코드
            document.getElementById('dongName').value = data.bname;

            document.getElementById("detailAddress").focus();
        }
    }).open();
}

document.getElementById('storeForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const userId = document.getElementById('userId').value;

    // CreateStoreRequest 구조 생성
    const payload = {
        storeCategoryId: document.getElementById('storeCategoryId').value,
        storeName: document.getElementById('storeName').value,
        businessNumber: document.getElementById('businessNumber').value,
        storePhone: document.getElementById('storePhone').value,
        description: document.getElementById('description').value,
        address: {
            sidoCode: document.getElementById('sidoCode').value,
            sidoName: document.getElementById('sidoName').value,
            sigunguCode: document.getElementById('sigunguCode').value,
            sigunguName: document.getElementById('sigunguName').value,
            dongCode: document.getElementById('dongCode').value,
            dongName: document.getElementById('dongName').value,
            roadAddress: document.getElementById('roadAddress').value,
            detailAddress: document.getElementById('detailAddress').value
        }
    };

    try {
        const response = await fetch('/api/v1/stores', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-Id': userId || '00000000-0000-0000-0000-000000000000' // 빈값일 때 기본값
            },
            body: JSON.stringify(payload)
        });

        const result = await response.json();
        if (response.ok) {
            alert('가게 등록 성공! 🥔');
            console.log(result);
        } else {
            alert('등록 실패: ' + result.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('서버 연결 오류');
    }
});