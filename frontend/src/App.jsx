import { useState, useRef, useEffect } from 'react';
import { Mic, MicOff, Search, Loader2, Store, Clock, Star, MapPin } from 'lucide-react';

export default function App() {
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [aiMessage, setAiMessage] = useState('');
  const [menus, setMenus] = useState([]);
  
  const recognitionRef = useRef(null);

  useEffect(() => {
    // Web Speech API STT 초기화
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRecognition) {
      const recognition = new SpeechRecognition();
      recognition.lang = 'ko-KR';
      recognition.interimResults = false;
      recognition.maxAlternatives = 1;

      recognition.onstart = () => {
        setIsListening(true);
        setError('');
        setAiMessage('');
        setMenus([]);
      };

      recognition.onresult = async (event) => {
        const text = event.results[0][0].transcript;
        setTranscript(text);
        await fetchRecommendation(text);
      };

      recognition.onerror = (event) => {
        console.error('STT Error:', event.error);
        if (event.error !== 'aborted') {
          setError(`음성 인식 오류: ${event.error}`);
        }
        setIsListening(false);
      };

      recognition.onend = () => {
        setIsListening(false);
      };

      recognitionRef.current = recognition;
    } else {
      setError('이 브라우저는 음성 인식을 지원하지 않습니다.');
    }

    return () => {
      if (recognitionRef.current) {
        recognitionRef.current.abort();
      }
      window.speechSynthesis.cancel();
    };
  }, []);

  const toggleListen = () => {
    if (isListening) {
      recognitionRef.current?.stop();
    } else {
      recognitionRef.current?.start();
    }
  };

  const speakText = (text) => {
    // 음성 출력 기능 제거 (사용자 요청)
    /*
    if (!window.speechSynthesis) return;
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'ko-KR';
    utterance.rate = 1.0;
    utterance.pitch = 1.1;
    window.speechSynthesis.speak(utterance);
    */
  };

  const fetchRecommendation = async (text) => {
    setIsLoading(true);
    try {
      const response = await fetch('/api/v1/recommendations/voice', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ transcribedText: text }),
      });
      
      if (!response.ok) throw new Error('서버 응답 오류 (데이터베이스 연동이 안되어있을 수 있습니다.)');
      
      const data = await response.json();
      setAiMessage(data.aiMessage);
      setMenus(data.recommendedMenus || []);
      speakText(data.aiMessage);
    } catch (err) {
      console.error(err);
      setError('추천 서버와 통신하는데 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', padding: '2rem', width: '100%' }}>
      <header style={{ textAlign: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1a1a1a', margin: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
          <Search size={32} color="#e67e22" />
          오늘 뭐 먹지?
        </h1>
        <p style={{ color: '#666', marginTop: '0.5rem' }}>말씀해 주시면 딱 맞는 메뉴를 찾아드릴게요!</p>
      </header>

      {/* Voice Control Card */}
      <div style={{ backgroundColor: 'white', borderRadius: '16px', padding: '2rem', boxShadow: '0 10px 25px rgba(0,0,0,0.05)', textAlign: 'center', marginBottom: '1.5rem', border: '1px solid #eee' }}>
        <button
          onClick={toggleListen}
          style={{
            backgroundColor: isListening ? '#ffebee' : '#e67e22',
            color: isListening ? '#d32f2f' : 'white',
            border: `2px solid ${isListening ? '#ffcdd2' : '#d35400'}`,
            borderRadius: '50%',
            width: '80px',
            height: '80px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            margin: '0 auto 1.5rem',
            cursor: 'pointer',
            transition: 'all 0.3s ease',
            boxShadow: isListening ? '0 0 0 8px rgba(211, 47, 47, 0.1)' : '0 4px 10px rgba(230, 126, 34, 0.3)'
          }}
        >
          {isListening ? <MicOff size={36} /> : <Mic size={36} />}
        </button>
        
        <h3 style={{ margin: '0 0 0.5rem 0', color: '#333' }}>
          {isListening ? '듣고 있어요...' : '버튼을 눌러 말하기'}
        </h3>
        
        {transcript && (
          <div style={{ marginTop: '1rem', padding: '1rem', backgroundColor: '#f8f9fa', borderRadius: '8px', color: '#495057', fontSize: '1.1rem', fontStyle: 'italic' }}>
            "{transcript}"
          </div>
        )}
        
        {error && <div style={{ color: '#e74c3c', marginTop: '1rem', fontSize: '0.9rem' }}>{error}</div>}
      </div>

      {/* Result Card */}
      {(isLoading || aiMessage) && (
        <div style={{ backgroundColor: 'white', borderRadius: '16px', padding: '1.5rem', boxShadow: '0 10px 25px rgba(0,0,0,0.05)', border: '1px solid #eee', animation: 'fadeIn 0.5s fade-in' }}>
          {isLoading ? (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '2rem 0', color: '#666' }}>
              <Loader2 className="animate-spin" size={32} style={{ marginBottom: '1rem', color: '#e67e22', animation: 'spin 1s linear infinite' }} />
              <span>AI가 취향을 분석하고 있습니다...</span>
              <style>{`@keyframes spin { 100% { transform: rotate(360deg); } }`}</style>
            </div>
          ) : (
            <>
              {/* AI Response Message */}
              <div style={{ backgroundColor: '#fff3e0', borderLeft: '4px solid #ff9800', padding: '1rem', borderRadius: '0 8px 8px 0', marginBottom: '1.5rem' }}>
                <p style={{ margin: 0, fontSize: '1.1rem', color: '#e65100', fontWeight: '500', lineHeight: 1.5 }}>
                  {aiMessage}
                </p>
              </div>

              {/* Menu List */}
              {menus.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {menus.map((menu, idx) => (
                    <div key={idx} style={{ padding: '1rem', border: '1px solid #e0e0e0', borderRadius: '12px', transition: 'transform 0.2s', ':hover': { transform: 'translateY(-2px)' } }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                        <h4 style={{ margin: 0, fontSize: '1.15rem', color: '#2c3e50', display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <Store size={18} color="#7f8c8d" />
                          {menu.storeName}
                        </h4>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '4px', backgroundColor: '#fff9c4', padding: '4px 8px', borderRadius: '12px', fontSize: '0.85rem', fontWeight: 'bold', color: '#f39c12' }}>
                          <Star size={14} fill="#f39c12" />
                          {menu.rating.toFixed(1)}
                        </div>
                      </div>
                      
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                        <div>
                          <p style={{ margin: '0 0 4px', fontWeight: 'bold', fontSize: '1.1rem', color: '#111' }}>{menu.menuName}</p>
                          <p style={{ margin: 0, fontSize: '0.9rem', color: '#7f8c8d', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>{menu.description}</p>
                        </div>
                        <span style={{ fontSize: '1.2rem', fontWeight: '900', color: '#e74c3c', whiteSpace: 'nowrap', marginLeft: '1rem' }}>
                          {menu.price.toLocaleString()}원
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ textAlign: 'center', padding: '2rem', color: '#95a5a6' }}>
                  조건에 맞는 요리를 찾지 못했어요.
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
}
