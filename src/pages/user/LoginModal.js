import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../../css/login/LoginModal.css'; // 모달 스타일을 위한 CSS 파일
import googleLogo from '../../images/google_logo.png';
import Logo from '../../images/LOGO.png';

const LoginModal = ({ isOpen, onClose }) => {
    const navigate = useNavigate();

    if (!isOpen) return null;

    const handleGoogleLogin = () => {
        const clientId = '493235437055-i3vpr6aqus0mqfarsvfm65j2rkllo97t.apps.googleusercontent.com';
        // 백엔드 도메인 + '/auth/google/callback' 로 설정
        const redirectUri = process.env.REACT_APP_BACKEND_URL + '/auth/google/callback';
        const scope = 'profile email';
        const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code&scope=${scope}`;
        window.location.href = authUrl;
    };

    const handleClose = () => {
        onClose(); // 모달 닫기
        navigate('/'); // 랜딩페이지로 이동
    };

    return (
        <div className="WS-login" onClick={handleClose}>
            <div className='WS-login-header-Container'>
                <button className='WS-login-back-button' onClick={handleClose}>&lt;</button>
            </div>

            <div className="WS-login-body-container" onClick={e => e.stopPropagation()}>

                <div className='WS-login-logo'>
                    <img src={Logo} alt="Logo" />
                </div>

                <div className='WS-login-button' onClick={handleGoogleLogin}>
                    <img src={googleLogo} alt="Google Logo" />
                    <span>구글 계정으로 시작하기</span>
                </div>

                <div className='WS-login-message-container'>
                    <div>개인정보방침</div>
                    <div>이용약관</div>
                </div>
            </div>
        </div>
    );
};

export default LoginModal;