import React, { useState } from 'react';
import LoginModal from './LoginModal';

function Login() {
    const [isModalOpen, setModalOpen] = useState(true);

    /* const openLoginModal = () => {
         setModalOpen(true);
     }; */

    const closeModal = () => {
        setModalOpen(false);
    };

    return (
        <div>
            <LoginModal isOpen={isModalOpen} onClose={closeModal} />
        </div>
    )
}

export default Login;
