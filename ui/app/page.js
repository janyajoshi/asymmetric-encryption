"use client";
import Image from "next/image";
import styles from "./page.module.css";
import { useState } from "react";
import axios from "axios";

export default function Home() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const fetchPublicKeyPem = async () => {
    const res = await fetch("/public_key.pem");
    const pem = await res.text();
    return pem;
  };

  const loadPublicKey = async (publicKey) => {
    const cleaned = publicKey
      .replace(/-----BEGIN PUBLIC KEY-----/, "")
      .replace(/-----END PUBLIC KEY-----/, "")
      .replace(/\s/g, "");

    const binaryDer = Uint8Array.from(atob(cleaned), (c) => c.charCodeAt(0));

    return await window.crypto.subtle.importKey(
      "spki",
      binaryDer.buffer,
      { name: "RSA-OAEP", hash: "SHA-256" },
      false,
      ["encrypt"]
    );
  };

  const encryptWithPublicKey = async (text, loadedPublicKey) => {
    const encoded = new TextEncoder().encode(text);
    const encrypted = await window.crypto.subtle.encrypt(
      { name: "RSA-OAEP" },
      loadedPublicKey,
      encoded
    );
    return btoa(String.fromCharCode(...new Uint8Array(encrypted)));
  };

  const submit = async () => {
    const publicKey = await fetchPublicKeyPem();
    const loadedPublicKey = await loadPublicKey(publicKey);
    axios.post("http://localhost:8080/api/readCredentials", {
      username: await encryptWithPublicKey(username, loadedPublicKey),
      password: await encryptWithPublicKey(password, loadedPublicKey),
    });
  };

  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            submit();
          }}
        >
          <label for="username">Username:</label>
          <input
            type="text"
            id="username"
            name="username"
            required
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <br />
          <br />

          <label for="password">Password:</label>
          <input
            type="password"
            id="password"
            name="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <br />
          <br />

          <button type="submit">Login</button>
        </form>
        <Image
          className={styles.logo}
          src="/next.svg"
          alt="Next.js logo"
          width={180}
          height={38}
          priority
        />
        <ol>
          <li>
            Get started by editing <code>app/page.js</code>.
          </li>
          <li>Save and see your changes instantly.</li>
        </ol>

        <div className={styles.ctas}>
          <a
            className={styles.primary}
            href="https://vercel.com/new?utm_source=create-next-app&utm_medium=appdir-template&utm_campaign=create-next-app"
            target="_blank"
            rel="noopener noreferrer"
          >
            <Image
              className={styles.logo}
              src="/vercel.svg"
              alt="Vercel logomark"
              width={20}
              height={20}
            />
            Deploy now
          </a>
          <a
            href="https://nextjs.org/docs?utm_source=create-next-app&utm_medium=appdir-template&utm_campaign=create-next-app"
            target="_blank"
            rel="noopener noreferrer"
            className={styles.secondary}
          >
            Read our docs
          </a>
        </div>
      </main>
      <footer className={styles.footer}>
        <a
          href="https://nextjs.org/learn?utm_source=create-next-app&utm_medium=appdir-template&utm_campaign=create-next-app"
          target="_blank"
          rel="noopener noreferrer"
        >
          <Image
            aria-hidden
            src="/file.svg"
            alt="File icon"
            width={16}
            height={16}
          />
          Learn
        </a>
        <a
          href="https://vercel.com/templates?framework=next.js&utm_source=create-next-app&utm_medium=appdir-template&utm_campaign=create-next-app"
          target="_blank"
          rel="noopener noreferrer"
        >
          <Image
            aria-hidden
            src="/window.svg"
            alt="Window icon"
            width={16}
            height={16}
          />
          Examples
        </a>
        <a
          href="https://nextjs.org?utm_source=create-next-app&utm_medium=appdir-template&utm_campaign=create-next-app"
          target="_blank"
          rel="noopener noreferrer"
        >
          <Image
            aria-hidden
            src="/globe.svg"
            alt="Globe icon"
            width={16}
            height={16}
          />
          Go to nextjs.org →
        </a>
      </footer>
    </div>
  );
}
