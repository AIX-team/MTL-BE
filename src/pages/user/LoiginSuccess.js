import { useEffect } from "react";
import { useLocation, useNavigate } from 'react-router-dom';
import '../../css/login/LoginSuccess.css';

function LoginSuccess() {
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const token = params.get('token');
        const error = params.get('error');

        if (token) {
            // 토큰이 있으면 저장하고 /link로 이동
            localStorage.setItem("token", "Bearer " + token);
            navigate('/link');
        } else if (error) {
            // 에러가 있으면 에러 메시지 표시
            let errorMessage = "로그인에 실패했습니다.";
            if (error === 'token') errorMessage = "토큰 발급에 실패했습니다.";
            if (error === 'user') errorMessage = "사용자 정보를 가져오는데 실패했습니다.";
            alert(errorMessage);
            navigate('/login');
        } else {
            // 토큰도 에러도 없으면 로그인 페이지로
            navigate('/login');
        }
    }, [location, navigate]);

    return (
        <div className="login-success-loading">
            <h2>로그인 처리 중...</h2>
        </div>
    );
}

export default LoginSuccess;